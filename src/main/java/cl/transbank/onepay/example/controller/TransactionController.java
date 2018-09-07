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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller
public class TransactionController {
    @Autowired private Cart cart;

    @PostMapping(value = "/transaction-create")
    @ResponseBody
    public String transactionCreate(@RequestParam("channel") String channel, HttpServletRequest request) throws AmountException {

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String path = request.getContextPath();
        if (!path.endsWith("/")) path += "/";
        String callbackUrl = String.format(
                "%s://%s:%s%stransaction-commit.html",
                scheme, serverName, serverPort, path);
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
        } catch (Exception e) {
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

    @GetMapping (value = "/transaction-commit")
    public ModelAndView transactionCommit(@RequestParam("occ") String occ,
                                          @RequestParam("externalUniqueNumber") String externalUniqueNumber,
                                          @RequestParam("status") String status) {
        Map<String, Object> model = new HashMap<String, Object>();

        if (null != status && !status.equalsIgnoreCase("PRE_AUTHORIZED")) {
            model.put("occ", occ);
            model.put("externalUniqueNumber", externalUniqueNumber);
            model.put("status", status);

            return new ModelAndView("transaction-error", model);
        }

        Onepay.setIntegrationType(Onepay.IntegrationType.TEST);

        // create options to send Onepay's keys
        Options options = Options.getDefaults()
                .setApiKey(ComerceConfig.ONEPAY_API_KEY)
                .setSharedSecret(ComerceConfig.ONEPAY_SHARED_SECRET);
        try {
            final TransactionCommitResponse response = Transaction.commit(occ, externalUniqueNumber, options);

            model.put("transaction", response);
            model.put("externalUniqueNumber", externalUniqueNumber);

            return new ModelAndView("transaction-success", "model", model);
        } catch (Exception e) {
            e.printStackTrace();
            return new ModelAndView("service-error", "message", e.getMessage());
        }
    }
}
