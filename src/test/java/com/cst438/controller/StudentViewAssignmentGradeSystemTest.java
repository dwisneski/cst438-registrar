package com.cst438.controller;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class StudentViewAssignmentGradeSystemTest {

    private static final String FRONTEND_URL = "http://localhost:5173";
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testInstructorCreatesAssignment_StudentSeesBlankScore() throws Exception {

        // ---------------------------------------------------------
        // 1. LOGIN AS INSTRUCTOR
        // ---------------------------------------------------------
        driver.get(FRONTEND_URL);

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        email.clear();
        email.sendKeys("ted@csumb.edu");

        WebElement password = driver.findElement(By.id("password"));
        password.clear();
        password.sendKeys("ted2025");

        driver.findElement(By.id("loginButton")).click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Instructor Home')]")
        ));

        // ---------------------------------------------------------
        // 2. SELECT TERM (2026 Fall)
        // ---------------------------------------------------------
        WebElement year = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));
        year.clear();
        year.sendKeys("2026");

        WebElement semester = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("semester")));
        semester.clear();
        semester.sendKeys("Fall");

        driver.findElement(By.id("selectTermButton")).click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr/td[2][contains(text(),'cst599')]")
        ));

        // ---------------------------------------------------------
        // 3. CLICK Assignments LINK
        // ---------------------------------------------------------
        WebElement assignmentsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//tr[td[2][contains(text(),'cst599')]]//a[@id='assignmentsLink']")
        ));
        assignmentsLink.click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("addAssignmentButton")
        ));

        // ---------------------------------------------------------
        // 4. CLICK ADD ASSIGNMENT BUTTON
        // ---------------------------------------------------------
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("addAssignmentButton")
        ));
        addBtn.click();

        WebElement dialog = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("dialog[open]")
        ));

        // ---------------------------------------------------------
        // 5. CREATE ASSIGNMENT
        // ---------------------------------------------------------
        String title = "asmt" + new Random().nextInt(10000);

        WebElement titleBox = dialog.findElement(By.name("title"));
        WebElement dueDateBox = dialog.findElement(By.name("dueDate"));

        titleBox.click();
        titleBox.clear();
        titleBox.sendKeys(title);

        // use a valid Fall 2026 due date
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const input = arguments[0];" +
                        "const value = arguments[1];" +
                        "const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "nativeInputValueSetter.call(input, value);" +
                        "input.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "input.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                dueDateBox,
                "2026-10-20"
        );

        System.out.println("Due date field value = " + dueDateBox.getAttribute("value"));
        Thread.sleep(2000);

        dialog.findElement(By.xpath(".//button[text()='Save']")).click();
        Thread.sleep(2000);

        dialog.findElement(By.xpath(".//button[text()='Close']")).click();
        Thread.sleep(1000);

        // ---------------------------------------------------------
        // 6. VERIFY ASSIGNMENT (INSTRUCTOR VIEW)
        // ---------------------------------------------------------
        WebElement instructorRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(),'" + title + "')]]")
        ));
        assertNotNull(instructorRow);

        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("homeLink")
        ));
        homeLink.click();
        Thread.sleep(1000);

        // ---------------------------------------------------------
        // 7. LOG OUT
        // ---------------------------------------------------------
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("logoutLink")
        ));
        logoutLink.click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("loginButton")
        ));

        // ---------------------------------------------------------
        // 8. LOGIN AS STUDENT
        // ---------------------------------------------------------
        WebElement email2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        email2.clear();
        email2.sendKeys("sam1@csumb.edu");

        WebElement password2 = driver.findElement(By.id("password"));
        password2.clear();
        password2.sendKeys("sam2025");

        driver.findElement(By.id("loginButton")).click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Student')]")
        ));

        // ---------------------------------------------------------
        // 9. STUDENT LOADS ASSIGNMENTS
        // ---------------------------------------------------------
        WebElement viewAssignmentsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("View Assignments")
        ));
        viewAssignmentsLink.click();
        Thread.sleep(1000);

        WebElement studentYear = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));
        studentYear.clear();
        studentYear.sendKeys("2026");

        WebElement studentSemester = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("semester")
        ));
        studentSemester.clear();
        studentSemester.sendKeys("Fall");

        WebElement getAssignmentsBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Get Assignments')]")
        ));
        getAssignmentsBtn.click();
        Thread.sleep(1000);

        // ---------------------------------------------------------
        // 10. VERIFY ASSIGNMENT + BLANK SCORE
        // ---------------------------------------------------------
        WebElement studentRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(),'" + title + "')]]")
        ));

        WebElement scoreCell = studentRow.findElement(By.xpath("./td[last()]"));

        assertEquals("N/A", scoreCell.getText().trim(),
                "Score should be blank for newly created assignment.");
    }
}