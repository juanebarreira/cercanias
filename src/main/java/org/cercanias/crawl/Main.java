package org.cercanias.crawl;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main {
    private static final String URL = "https://www.renfe.com/es/es/cercanias/cercanias-madrid/horarios";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static boolean initialized = false;

    public static synchronized void init() {
        if (!initialized) {
            initializeDriver();
            initialized = true;
        }
    }

    public static synchronized void cleanup() {
        if (initialized && driver != null) {
            driver.quit();
            initialized = false;
        }
    }

    public static List<Train> searchTrainsInRange(String origin, String destination) throws InterruptedException {
        try {
            init();  // Asegurar que el driver está inicializado
            String startTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            return searchTrainsInRange(origin, destination, startTime);
        } finally {
            cleanup();  // Limpiar recursos después de la búsqueda
        }
    }

    public static List<Train> searchTrainsInRange(String origin, String destination, String startTime) throws InterruptedException {
        try {
            init();
            String endTime = LocalTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"));
            return searchTrainsInRange(origin, destination, startTime, endTime);
        } finally {
            cleanup();
        }
    }

    public static List<Train> searchTrainsInRange(String origin, String destination, String startTime, String endTime) throws InterruptedException {
        try {
            init();
            return searchTrainsInRange(origin, destination, startTime, endTime, null);
        } finally {
            cleanup();
        }
    }

    public static List<Train> searchTrainsInRange(String origin, String destination, String startTime, String endTime, String filterTimeInMinutes) throws InterruptedException {
        try {
            init();
            List<Train> trainsInRange = new ArrayList<>();
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

            return trainsInRange;
        } finally {
            cleanup();
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
}