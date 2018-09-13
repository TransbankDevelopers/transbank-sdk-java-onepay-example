package cl.transbank.onepay.example.test;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.junit.ScreenShooter;
import org.junit.*;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Configuration.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.WebDriverRunner.closeWebDriver;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OnepayExampleSelenideTest {
    private static final HttpUtil httpUtil = OnepayHttpUtil.getInstance();
    @Rule
    public ScreenShooter screenShooter = ScreenShooter.failedTests();

    @BeforeClass
    public static void setup() {
        timeout = 20000;
        baseUrl = "http://localhost:8080";
        startMaximized = true;

        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    }

    @Before
    public void resetBrowser() {
        open("/");
    }

    @Test
    public void testCheckout() throws IOException {
        $(byId("do-checkout")).click();

        final SelenideElement iframe = $(byClassName("zoid-component-frame"));
        final String iframeName = iframe.getAttribute("name");

        switchTo().innerFrame(iframeName);
        final SelenideElement qrCodeText = $(byClassName("onepay-payment-qr-code-text"));
        qrCodeText.shouldHave(text("Código de compra"));
        final String text = qrCodeText.getText();

        // find the OTT
        Pattern pattern = Pattern.compile("([0-9]+ - [0-9]+)");
        final Matcher matcher = pattern.matcher(text);
        Assert.assertTrue(matcher.find());
        final String ott = matcher.group(1).replaceAll(" - ", "");

        // If attach transaction before websocket is currently connected it never receive the messages and
        // transaction can not be completed
        sleep(3000);

        // PRE_AUTHORIZE TRANSACTION
        authorize(ott);

        // COMMIT
        String commitTransactionResponse = commit(ott);

        assertNotNull(commitTransactionResponse);
        assertEquals("{\"status\":\"OK\",\"message\":\"Se ha actualizado exitosamente la transacción\"}", commitTransactionResponse);

        switchTo().parentFrame();

        postPayment();
    }

    @Test
    public void testDirectQr() throws IOException {
        $(byId("do-direct-qr")).click();
        final String ott = $(byId("rendered-ott")).getText();

        sleep(4000);

        // PRE_AUTHORIZE TRANSACTION
        authorize(ott);

        // COMMIT
        String commitTransactionResponse = commit(ott);

        assertNotNull(commitTransactionResponse);
        assertEquals("{\"status\":\"OK\",\"message\":\"Se ha actualizado exitosamente la transacción\"}", commitTransactionResponse);

        postPayment();
    }

    private static void authorize(String ott) throws IOException {
        httpUtil.request(new URL("https://onepay.ionix.cl/mobile-payment-emulator/home/attachTransaction"),
                HttpUtil.RequestMethod.POST,
                String.format("username=%s&buyOrder=%s", "test@onepay.cl", ott),
                HttpUtil.ContentType.X_WWW_FORM_URLENCODED);
    }

    private static String commit(String ott) throws IOException {
        return httpUtil.request(new URL("https://onepay.ionix.cl/mobile-payment-emulator/home/updateTransaction"),
                HttpUtil.RequestMethod.POST,
                String.format("status=PRE_AUTHORIZED&installments=1&buyOrder=%s", ott),
                HttpUtil.ContentType.X_WWW_FORM_URLENCODED);
    }

    private static void postPayment() {
        // refund
        $("body").shouldHave(text("Tu pago se completo en forma exitosa"));

        // We could not see the commit transaction if we do not wait 2 seconds before refund
        sleep(2000);

        $(byText("Anular esta compra")).click();

        $("body").shouldHave(text("Transacción anulada en forma exitosa!"));

        // We could not see when refund was success so we wait 2 seconds until close browser
        sleep(2000);
    }

    @AfterClass
    public static void logout() {
        closeWebDriver();
    }
}
