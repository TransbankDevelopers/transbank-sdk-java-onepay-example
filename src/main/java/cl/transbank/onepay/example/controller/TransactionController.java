package cl.transbank.onepay.example.controller;

import cl.transbank.onepay.Onepay;
import cl.transbank.onepay.example.ComerceConfig;
import cl.transbank.onepay.example.model.Product;
import cl.transbank.onepay.example.resource.Cart;
import cl.transbank.onepay.exception.AmountException;
import cl.transbank.onepay.model.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TransactionController {
    @Autowired private Cart cart;

    @RequestMapping(value = "/transaction-create", method = RequestMethod.POST)
    @ResponseBody
    public String transactionCreate(@RequestParam("channel") String channel) throws AmountException, UnknownHostException, SocketException {
        String callbackUrl = String.format("http://%s:8081/onepay-sdk-example/transaction-commit.html", System.getenv("HOST_IP"));
        Onepay.setCallbackUrl(callbackUrl);
        Onepay.setIntegrationType(Onepay.IntegrationType.TEST);

        List<Product> products = cart.getProducts();

        // create sdk shopping cart
        ShoppingCart shoppingCart = new ShoppingCart();

        // add items to the shopping cart
        for (Product product : products) {
            Item item = new Item(product.getName(), product.getQuantity(), product.getPrice(),null,-1L);
            shoppingCart.add(item);
        }

        // create options to send Onepay's keys
        Options options = Options.getDefaults()
                .setApiKey(ComerceConfig.ONEPAY_API_KEY)
                .setSharedSecret(ComerceConfig.ONEPAY_SHARED_SECRET);

        // transaction create on Onepay
        TransactionCreateResponse response = null;
        try {
            response = Transaction.create(shoppingCart, Enum.valueOf(Onepay.Channel.class, channel), options);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Map<String, Object> toJson = new HashMap<>();
        toJson.put("occ", response.getOcc());
        toJson.put("ott", response.getOtt());
        toJson.put("externalUniqueNumber", response.getExternalUniqueNumber());
        toJson.put("qrCodeAsBase64", response.getQrCodeAsBase64());
        toJson.put("issuedAt", response.getIssuedAt());
        toJson.put("amount", shoppingCart.getTotal());

        return new Gson().toJson(toJson);
    }

    @RequestMapping (value = "/transaction-commit", method = RequestMethod.GET)
    public ModelAndView transactionCommit(@RequestParam("occ") String occ,
                                          @RequestParam("externalUniqueNumber") String externalUniqueNumber) {
        Onepay.setIntegrationType(Onepay.IntegrationType.TEST);

        // create options to send Onepay's keys
        Options options = Options.getDefaults()
                .setApiKey(ComerceConfig.ONEPAY_API_KEY)
                .setSharedSecret(ComerceConfig.ONEPAY_SHARED_SECRET);
        try {
            final TransactionCommitResponse response = Transaction.commit(occ, externalUniqueNumber, options);
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("transaction", response);
            model.put("externalUniqueNumber", externalUniqueNumber);
            return new ModelAndView("transaction-success", "model", model);
        } catch (Throwable e) {
            e.printStackTrace();
            return new ModelAndView("service-error", "message", e.getMessage());
        }
    }
}
