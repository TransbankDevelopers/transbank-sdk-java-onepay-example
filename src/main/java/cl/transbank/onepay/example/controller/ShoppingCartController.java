package cl.transbank.onepay.example.controller;

import cl.transbank.onepay.example.resource.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ShoppingCartController {
    @Autowired private Cart cart;

    @RequestMapping("/index")
    public ModelAndView shoppingCart() {
        return new ModelAndView("shoppingcart", "products",
                cart.getProducts());
    }
}
