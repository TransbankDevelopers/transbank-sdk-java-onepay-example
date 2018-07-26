package cl.transbank.onepay.example.resource;

import cl.transbank.onepay.example.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component("cart")
public class Cart {
    private Gson gson;

    private static final String JSON_FILE = "/cart-products.json";

    public Cart() {
        super();
        gson = new Gson();
    }

    public List<Product> getProducts() {
        InputStream json = Cart.class.getResourceAsStream(Cart.JSON_FILE);
        return gson.fromJson(new InputStreamReader(json), new TypeToken<List<Product>>(){}.getType());
    }
}
