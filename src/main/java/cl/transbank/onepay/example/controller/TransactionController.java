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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller
public class TransactionController {
    @Autowired private Cart cart;
    @PostMapping(
        value = "/transaction-create", 
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

        try {
            final TransactionCommitResponse response = Transaction.commit(occ, externalUniqueNumber);

            model.put("transaction", response);
            model.put("externalUniqueNumber", externalUniqueNumber);

            return new ModelAndView("transaction-success", "model", model);
        } catch (Exception e) {
            e.printStackTrace();
            return new ModelAndView("service-error", "message", e.getMessage());
        }
    }
}
