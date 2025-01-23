package org.cercanias.crawl;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main {
    private static final String URL = "https://www.renfe.com/es/es/cercanias/cercanias-madrid/horarios";
    private static WebDriver driver;
    private static WebDriverWait wait;

    public static void main(String[] args) {
        try {
            initializeDriver();
            List<Train> trains = searchTrainsInRange("Aravaca", "Recoletos", "08:30", "9:00", "35");

            StringBuilder message = new StringBuilder("🚂 Trenes disponibles:\n\n");
            trains.forEach(train -> message.append(train.telegramFormat()));
            boolean toTelegram = System.getenv("TO_TELEGRAM") != null;
            if(toTelegram) {
                String token = System.getenv("BOT_TOKEN");
                String chatId = System.getenv("CHAT_ID");
                if (token == null || chatId == null) {
                    throw new RuntimeException("BOT_TOKEN y CHAT_ID deben estar configurados en las variables de entorno");
                }
                TelegramNotifier telegramNotifier = new TelegramNotifier(token, chatId);
                telegramNotifier.sendMessage(message.toString());
            }else{
                System.out.println(message.toString());
            }
        } catch (Exception e) {
            System.err.println("Error en la ejecución: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static void initializeDriver() {
        ChromeOptions options = new ChromeOptions();

        // Configuraciones básicas
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-web-security");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--window-size=1920,1080");

        // Detectar si estamos en Docker
        boolean isDocker = System.getenv("DOCKER_ENV") != null;

        try {
            if (isDocker) {
                String chromeBinary = System.getenv("CHROME_BIN");
                if (chromeBinary != null) {
                    options.setBinary(chromeBinary);
                }

                ChromeDriverService service = new ChromeDriverService.Builder()
                        .usingDriverExecutable(new File(System.getenv("CHROMEDRIVER_PATH")))
                        .usingAnyFreePort()
                        .build();

                driver = new ChromeDriver(service, options);
            } else {
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver(options);
            }

            // Configurar timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

            wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        } catch (Exception e) {
            System.err.println("Error inicializando el driver: " + e.getMessage());
            if (driver != null) {
                driver.quit();
            }
            throw e;
        }
    }

    private static List<Train> searchTrainsInRange(String origin, String destination, String startTime, String endTime) throws InterruptedException {
        return searchTrainsInRange(origin, destination, startTime, endTime, null);
    }

    private static List<Train> searchTrainsInRange(String origin, String destination, String startTime, String endTime, String filterTimeInMinutes) throws InterruptedException {
        List<Train> trainsInRange = new ArrayList<>();
        try {
            driver.get(URL);
            System.out.println("Navigating to: " + URL);

            // 1. Reducir espera inicial a 2s
            Thread.sleep(2000);

            // Wait for page to load completely
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
            System.out.println("Page loaded completely");

            // 2. Optimizar manejo de cookies - reducir esperas
            try {
                WebElement cookiesButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.id("onetrust-accept-btn-handler")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cookiesButton);
                System.out.println("Cookies accepted");
                Thread.sleep(1000); // Reducido de 3s a 1s
            } catch (TimeoutException e) {
                System.out.println("Cookie dialog not found or already accepted");
            }

            // Esperar a que el iframe esté presente y cambiar a él
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("horariosCercanias")));
            driver.switchTo().frame("horariosCercanias");
            System.out.println("Switched to iframe");

            // Esperar a que el formulario esté cargado
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
            System.out.println("Form found");

            takeScreenshot("before_select.png");

            // 3. Optimizar selección de origen y destino - reducir esperas
            // Seleccionar origen
            Select originSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.name("o"))));
            originSelect.selectByVisibleText(origin + " ");
            System.out.println("Origin selected: " + origin);
            Thread.sleep(500); // Reducido de 2s a 500ms

            // Seleccionar destino
            Select destinationSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.name("d"))));
            destinationSelect.selectByVisibleText(destination + " ");
            System.out.println("Destination selected: " + destination);
            takeScreenshot("after_select.png");
            Thread.sleep(500); // Reducido de 2s a 500ms

            // 4. Optimizar proceso de búsqueda
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a.irf-search-nearness__btn")));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click(); " +
                            "arguments[0].dispatchEvent(new Event('click', { bubbles: true }));",
                    searchButton
            );
            System.out.println("Search button clicked");
            Thread.sleep(1000); // Reducido de 3s a 1s

            // Esperar a que desaparezca cualquier loading
            try {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector(".loading, .spinner, .loader")
                ));
            } catch (Exception e) {
                System.out.println("No loading indicator found");
            }

            // 5. Optimizar espera de resultados
            WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("tablaHorarios")));

            // Reducir espera final a 500ms
            Thread.sleep(500);

            // List to store results
            List<Train> trains = new ArrayList<>();

            // Get all table rows
            List<WebElement> rows = driver.findElements(By.cssSelector("#tablaHorarios tbody tr"));

            // Extract information from each row
            for (WebElement row : rows) {
                try {
                    Map<String, String> train = new HashMap<>();
                    String line = row.findElement(By.cssSelector("td[name='codLinea'] span")).getText();
                    String departureTime = row.findElements(By.cssSelector("td")).get(2).getText();
                    String arrivalTime = row.findElements(By.cssSelector("td")).get(3).getText();
                    String travelTime = row.findElement(By.cssSelector("span[id^='idTiempo_']")).getText();
                    String trainCode = row.findElement(By.cssSelector("span[id^='codigoTren_']")).getText();

                    trains.add(new Train(line, origin, destination, departureTime, arrivalTime, travelTime, trainCode));

                } catch (Exception e) {
                    System.out.println("Error processing row: " + e.getMessage());
                    continue;
                }
            }
            trainsInRange = trains.stream()
                    .filter(train -> train.departsInRange(startTime, endTime))
                    .filter(train -> train.takesLessThan(filterTimeInMinutes))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            takeScreenshot("error_screenshot.png");
            throw e;
        }
        return trainsInRange;
    }

    private static void takeScreenshot(String filename) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("/app/screenshots/" + filename);
            Files.copy(screenshot.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved as: " + destFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error taking screenshot: " + e.getMessage());
        }
    }

    public static void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}