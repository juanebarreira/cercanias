package org.cercanias.crawl;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.time.*;
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
            LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Europe/Madrid"));
            String startTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            System.out.println("Searching for: " + origin + " to " + destination+ " in " + startTime);
            return searchTrainsInRange(origin, destination, startTime);
        } finally {
            cleanup();  // Limpiar recursos después de la búsqueda
        }
    }

    public static List<Train> searchTrainsInRange(String origin, String destination, String startTime) throws InterruptedException {
        try {
            init();
            LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Europe/Madrid")).plusHours(1);
            String endTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
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
            
            try {
                driver.get(URL);
                System.out.println("Navigating to: " + URL);

                // Espera mejorada para la carga inicial de la página
                wait.until(webDriver -> {
                    try {
                        String readyState = ((JavascriptExecutor) webDriver).executeScript("return document.readyState").toString();
                        boolean isJQueryComplete = (Boolean) ((JavascriptExecutor) webDriver)
                            .executeScript("return (typeof jQuery !== 'undefined') ? jQuery.active == 0 : true");
                        boolean areElementsPresent = !webDriver.findElements(By.tagName("body")).isEmpty();
                        
                        System.out.println("Page state: " + readyState + 
                                         ", jQuery complete: " + isJQueryComplete + 
                                         ", Elements present: " + areElementsPresent);
                        
                        return readyState.equals("complete") && isJQueryComplete && areElementsPresent;
                    } catch (Exception e) {
                        return false;
                    }
                });
                System.out.println("Page fully loaded and interactive");


                // Espera mejorada para el iframe

                Thread.sleep(3000);
                WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("horariosCercanias")));
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframe));
//                System.out.println("Switched to iframe");
//                takeScreenshot("page-loaded.png");


                // Espera mejorada para el formulario
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
//                System.out.println("Form found");
//                takeScreenshot("before_select.png");

                // Selección de origen
                WebElement originElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("o")));
                Select originSelect = new Select(originElement);
                String originValue = TrainStation.getValue(origin).orElse("");
                originSelect.selectByValue(originValue);
//                System.out.println("Origin selected: " + origin);

                // Selección de destino
                WebElement destinationElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("d")));
                Select destinationSelect = new Select(destinationElement);
                String destinationValue = TrainStation.getValue(destination).orElse("");

                destinationSelect.selectByValue(destinationValue);
//                System.out.println("Destination selected: " + destination);
//                takeScreenshot("after_select.png");

                // Mantener la referencia a originElement para el scroll posterior
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });",
                    originElement
                );
//                System.out.println("Scrolled to origin element");

                /*// Pequeña pausa para que el scroll termine
                wait.until(webDriver -> {
                    Long scrollY = (Long) ((JavascriptExecutor) webDriver)
                        .executeScript("return window.pageYOffset;");
                    return scrollY != 0;
                });*/
//                takeScreenshot("before_submit.png");
//                System.out.println("Remover - Antes de hacer click "+new Date());
                // Click mejorado en el botón de búsqueda
                WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a.irf-search-nearness__btn")));
//                System.out.println("Remover - Antes de esperar "+new Date());
                wait.until(ExpectedConditions.elementToBeClickable(searchButton));
//                System.out.println("Remover - Encontre. ahora a ejecutar "+new Date());
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click(); " +
                        "arguments[0].dispatchEvent(new Event('click', { bubbles: true }));",
                        searchButton
                );
//                System.out.println("Search button clicked");

//                takeScreenshot("after_submit.png");
//                System.out.println("Remover - Se clickeo. Ahora a esperar 1: "+new Date());

                // Espera mejorada para los resultados
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading")),
                    ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")),
                    ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loader"))
                ));
//                System.out.println("Remover - Se clickeo. Ahora a esperar 2: "+new Date());
                // Espera mejorada para la tabla de resultados
                wait.until(ExpectedConditions.and(
                    ExpectedConditions.presenceOfElementLocated(By.id("tablaHorarios")),
                    ExpectedConditions.visibilityOfElementLocated(By.id("tablaHorarios"))
                ));
//                System.out.println("Table found and visible");
//                System.out.println("Remover - Se clickeo. Ahora a esperar 3, ya esta la tablaHorarios: "+new Date());
//                takeScreenshot("with_results.png");

                // Espera a que los datos de la tabla estén cargados
                wait.until(webDriver -> !driver.findElements(By.cssSelector("#tablaHorarios tbody tr")).isEmpty());
//                System.out.println("Table rows loaded");

                List<WebElement> rows = driver.findElements(By.cssSelector("#tablaHorarios tbody tr"));
                List<Train> trains = new ArrayList<>();

                for (WebElement row : rows) {
                    try {
                        wait.until(ExpectedConditions.visibilityOf(row));
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

            } catch (TimeoutException e) {
                System.err.println("Timeout durante la operación: " + e.getMessage());
                takeScreenshot("timeout-error.png");
                throw e;
            }
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

        // Agregar estas opciones para ignorar errores de certificados
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--ignore-ssl-errors");
        options.setAcceptInsecureCerts(true);

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