package cn.timer;

import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static WebDriver driver;

    public static void main(String[] args) {
        if (args.length != 3) return;
        logger.setLevel(Level.INFO);

        logger.info("正在启动Edge...");
        EdgeOptions options = new EdgeOptions();
        if (args[2].equalsIgnoreCase("headless"))
            options.addArguments("--headless"); // 无头模式
        driver = new EdgeDriver(options);
        Actions actions = new Actions(driver);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.get("https://teacher.ewt360.com/");
        logger.info(driver.getTitle());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        logger.info("尝试登录...");
        WebElement userT = driver.findElement(By.id("login__password_userName"));
        userT.sendKeys(args[0]);
        WebElement passT = driver.findElement(By.id("login__password_password"));
        passT.sendKeys(args[1]);
        WebElement subBtn = driver.findElement(By.className("ant-btn-primary"));
        subBtn.click();

        WebElement myHoliday = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("我的假期")));
        myHoliday.click();

        driver.close();
        switchToNewTab();

        WebElement startBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("ant-btn-primary")));
        startBtn.click();

        logger.info("获取总完成度...");
        try {
            Thread.sleep(1000); // 等待1秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String totalProgress = getAllProgress().split("/")[0];
        String totalTasks = getAllProgress().split("/")[1];

        if (totalProgress.equals(totalTasks)) {
            logger.info("所有课程已完成,按下回车退出");
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            boolean turnLeft = true;
            boolean start = false;
            boolean confirmQuit = false;

            logger.info("寻找左滑按钮...");
            WebElement leftBtn = null;
            try {
                leftBtn = new WebDriverWait(driver, Duration.of(2, ChronoUnit.SECONDS))
                        .until(ExpectedConditions.elementToBeClickable(By.className("left-icon")));
            } catch (TimeoutException e) {
                logger.info("页面最左");
                turnLeft = false;
                start = true;
            }

            while (true) {
                if (turnLeft) {
                    try {
                        actions.click(leftBtn).perform();
                    } catch (Exception e) {
                        logger.info("页面最左");
                        turnLeft = false;
                        start = true;
                    }
                }

                if (start) {
                    logger.info("获取完成度...");
                    List<WebElement> datas = driver.findElement(By.className("swiper-item-box"))
                            .findElements(By.tagName("li"));

                    try {
                        Thread.sleep(1000); // 等待1秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < datas.size(); i++) {
                        String[] progress = getAllProgress().split("/");
                        if (progress[0].equals(progress[1])) {
                            logger.info("已完成所有课程!");
                            logger.info("回车退出");
                            start = false;
                            confirmQuit = true;
                            try {
                                System.in.read();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }

                        WebElement data = datas.get(i).findElement(By.className("day-card-container-19key"));
                        if (data.getText().isEmpty()) {
                            continue;
                        }

                        // logger.info(Arrays.toString(data.getText().toCharArray()));
                        // StringBuilder stringBuffer = new StringBuilder();
                        // for (char c : data.getText().toCharArray()) {
                        //     stringBuffer.append((int) c);
                        //     stringBuffer.append('.');
                        // }
                        // logger.info(stringBuffer.toString());

                        WebElement pdata = data.findElement(By.className("day-2xflZ"));
                        WebElement pdata2 = data.findElement(By.tagName("p"));
                        String day = pdata.getText();
                        String progress2 = pdata2.getText();
                        String[] progressSplit = progress2.split("/");

                        if (!progressSplit[0].equals(progressSplit[1])) {
                            logger.info(day + "的进度为" + progress2);
                            actions.click(data).perform();

                            try {
                                Thread.sleep(1000); // 等待1秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            List<WebElement> lessonList = driver.findElements(By.className("task-card-container-i009V"));

                            //if (lessonList.get(lessonList.size() - 1).getText().equals("加载更多")) {
                            //    logger.info("发现更多课程,重新获取...");
                            //    actions.click(lessonList.get(lessonList.size() - 1)).perform();
                            //    actions.moveToElement(data).perform();
                            //    lessonList = driver.findElements(By.className("task-card-container-i009V"));
                            //    lessonList.remove(lessonList.size() - 1);
                            //} else {
                            //    lessonList.remove(lessonList.size() - 1);
                            //}
                            logger.info(String.valueOf(lessonList.size()));

                            for (WebElement rawLesson : lessonList) {
                                try {
                                    logger.info("总进度: " + getAllProgress());
                                } catch (Exception e) {
                                    logger.info("获取进度失败");
                                }

                                String type = rawLesson.findElement(By.className("ewt-tag-wrap")).getText();
                                logger.info(type);
                                if (!type.startsWith("视频")) {
                                    logger.info("不是视频");
                                    continue;
                                }

                                String lessonName = rawLesson.findElement(By.className("title-2kzJs")).getText();
                                String lessonStat = rawLesson.findElement(By.className("operate-status-Z87Op")).getText();

                                logger.info(lessonName);
                                logger.info(lessonStat);

                                if (!lessonStat.contains("已完成")) {
                                    actions.click(rawLesson).perform();

                                    try {
                                        Thread.sleep(3000); // 等待3秒
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    switchToNewTab();

                                    if (driver.getCurrentUrl().contains("xinli.ewt360.com") ||
                                            driver.getCurrentUrl().contains("web.ewt360.com/spiritual-growth")) {
                                        try {
                                            Thread.sleep(5000); // 等待5秒
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        logger.info(lessonName + " | 已完成");
                                        driver.close();
                                        switchToNewTab();
                                    } else {
                                        try {
                                            Thread.sleep(1000); // 等待1秒
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        WebElement video = driver.findElement(By.tagName("video"));
                                        while (true) {
                                            try {
                                                Thread.sleep(1000); // 每隔5秒检查一次
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            ((JavascriptExecutor) driver).executeScript("document.querySelector('video').playbackRate = 2");
                                            ((JavascriptExecutor) driver).executeScript("document.querySelector('video').muted = true");

                                            List<WebElement> stupid = driver.findElements(By.className("btn-3LStS"));
                                            if (!stupid.isEmpty()) {
                                                actions.click(stupid.get(0)).perform();
                                                logger.info("EWT挂机检测");
                                            }

                                            Object currentTime = ((JavascriptExecutor) driver).executeScript("return arguments[0].currentTime", video);
                                            Object duration = ((JavascriptExecutor) driver).executeScript("return arguments[0].duration", video);

                                            logger.info(String.valueOf(currentTime));

                                            if (currentTime instanceof Long) {
                                                if ((long) currentTime >= (long) duration) {
                                                    logger.info(lessonName + " | 已完成");
                                                    driver.close();
                                                    switchToNewTab();
                                                    break;
                                                }
                                            }

                                            if (currentTime instanceof Double) {
                                                if ((double) currentTime >= (double) duration) {
                                                    logger.info(lessonName + " | 已完成");
                                                    driver.close();
                                                    switchToNewTab();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (i + 1 < datas.size() && datas.get(i + 1).getText().isEmpty()) {
                                logger.info("右滑");
                                WebElement rightBtn = driver.findElement(By.className("right-icon"));
                                actions.click(rightBtn).perform();
                                try {
                                    Thread.sleep(3000); // 等待3秒
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                } else if (confirmQuit) {
                    break;
                }
            }
        }
        driver.quit();
    }

    private static void switchToNewTab() {
        String newHandle = driver.getWindowHandles().toArray()[driver.getWindowHandles().size() - 1].toString();
        logger.info("切换到新页面: " + newHandle);
        driver.switchTo().window(newHandle);
    }

    private static String getAllProgress() {
        return driver.findElement(By.cssSelector("#rc-tabs-0-panel-1 > section > section > section > span:nth-child(1) > span")).getText();
    }
}