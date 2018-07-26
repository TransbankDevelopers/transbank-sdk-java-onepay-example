package cl.transbank.onepay.example.controller;

import cl.transbank.onepay.Onepay;
import cl.transbank.onepay.example.ComerceConfig;
import cl.transbank.onepay.example.model.Product;
import cl.transbank.onepay.example.resource.Cart;
import cl.transbank.onepay.exception.TransbankException;
import cl.transbank.onepay.model.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TransactionController {
    @Autowired private Cart cart;

    @RequestMapping(value = "/transaction-create", method = RequestMethod.POST)
    @ResponseBody
    public String transactionCreate() throws TransbankException, IOException {
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
        TransactionCreateResponse response = Transaction.create(shoppingCart, options);

        return new Gson().toJson(response);
    }

    @RequestMapping (value = "/transaction-commit", method = RequestMethod.POST)
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
