package cl.transbank.onepay.example.controller;

import cl.transbank.onepay.Onepay;
import cl.transbank.onepay.example.model.Product;
import cl.transbank.onepay.example.resource.Cart;
import cl.transbank.onepay.exception.AmountException;
import cl.transbank.onepay.model.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

import javax.servlet.http.HttpServletRequest;

@Controller
public class TransactionController {
    @Autowired private Cart cart;
    @RequestMapping(
            value = "/transaction-create", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String transactionCreate(@RequestParam("channel") String channel, HttpServletRequest request) throws AmountException {
        setOnepayCallbackUrlFromRequest(request);
        List<Product> products = cart.getProducts();

        // create sdk shopping cart
        ShoppingCart shoppingCart = new ShoppingCart();

        // add items to the shopping cart
        for (Product product : products) {
            Item item = new Item(product.getName(), product.getQuantity(), product.getPrice(), null, -1L);
            shoppingCart.add(item);
        }

        // transaction create on Onepay
        TransactionCreateResponse response = null;
        try {
            response = Transaction.create(shoppingCart, Enum.valueOf(Onepay.Channel.class, channel));
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

    private void setOnepayCallbackUrlFromRequest(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String path = request.getContextPath();
        if (!path.endsWith("/")) path += "/";
        String callbackUrl = String.format(
                "%s://%s:%s%stransaction-commit.html",
                scheme, serverName, serverPort, path);
        Onepay.setCallbackUrl(callbackUrl);
    }

    @RequestMapping (value = "/transaction-commit", method = RequestMethod.GET)
    public ModelAndView transactionCommit(@RequestParam("occ") String occ,
                                          @RequestParam("externalUniqueNumber") String externalUniqueNumber) {
        Onepay.setIntegrationType(Onepay.IntegrationType.TEST);

        try {
            final TransactionCommitResponse response = Transaction.commit(occ, externalUniqueNumber);
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
