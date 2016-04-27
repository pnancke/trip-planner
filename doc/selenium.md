# Selenium-UI Testframework

## Install
- download ChromeDriver from here: [ChromeDriver](https://sites.google.com/a/chromium.org/chromedriver/downloads)
- move driver to the specified location from here: [Requirements](https://github.com/SeleniumHQ/selenium/wiki/ChromeDriver#requirements)
(for Linux: `/usr/bin/google-chrome`
- make the `chromedriver` executable with `chmod +x`

## Usage

```
private WebDriver driver = new ChromeDriver();

public void open() throws Exception {
     driver.get("http://localhost:8080/home/index");
     driver.wait(10000L);
}
```
And in Test:

```
@Test
public void testButtonClick(){
    //this System-Property must be specified
    System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
    HomeSite home= new HomeSite();
        ...
```