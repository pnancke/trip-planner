package trip.planner

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

class HomeUI {

    private static final String BASE_HOME_URI = "http://localhost:8080/home/index"
    private WebDriver driver = new ChromeDriver();

    void open() {
        driver.get(BASE_HOME_URI)
    }

    void clickSubmitButton() {
        WebElement button = driver.findElement(By.id("submit-route-button"))
        button.click()
    }

    void fillStartField(String place) {
        WebElement startLocationField = driver.findElement(By.id("location-search-start"))
        startLocationField.sendKeys(place)
    }

    void fillDestinationField(String place) {
        WebElement startLocationField = driver.findElement(By.id("location-search-destination"))
        startLocationField.sendKeys(place)
    }
}
