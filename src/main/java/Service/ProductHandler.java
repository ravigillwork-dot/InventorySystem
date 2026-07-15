package Service;

import Model.Product;
import Repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductHandler implements HttpHandler {
    private final ProductRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductHandler(ProductRepository repo){
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method){
            case "GET" -> handleGet(exchange);
            case "POST" -> handlePost(exchange);
            case "DELETE" -> handleDelete(exchange);
            case "PUT" -> handlePut(exchange);
            default -> sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private Map<String,String> parseQuery(HttpExchange exchange){
        Map <String,String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isEmpty()){
            return params;
        }
        for (String pair : query.split("&")){
            String[] queryDetails = pair.split("=");
            if (queryDetails.length == 2){
                params.put(queryDetails[0], queryDetails[1]);
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, int statusCode , String body) throws IOException {
        byte[] bodyBytes = body.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode,bodyBytes.length);
        exchange.getResponseBody().write(bodyBytes);
        exchange.close();
    }

    private void handleGet(HttpExchange exchange) throws IOException{
       try {
           Map<String,String> params = parseQuery(exchange);
           if (!params.containsKey("productId")){
               List<Product> all = repo.findAll();
               sendResponse(exchange, 200, mapper.writeValueAsString(all));
               return;
           }
           Product p = repo.findById(Long.parseLong(params.get("productId")));
           if (p == null){
               sendResponse(exchange,404, "{\"error\":\"Product not found\"}");
               return;
           }
               sendResponse(exchange, 200, mapper.writeValueAsString(p));

       } catch (NumberFormatException e){
           sendResponse(exchange, 400,"{\"error\":\"Invalid Id\"}" );
       }
       catch (RepositoryException e){
           sendResponse(exchange, 500, "{\"error\":\"Something went wrong\"}");
       }catch (JsonProcessingException e){
           sendResponse(exchange, 500, "{\"error\":\"Invalid Json\"}");
       }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            Product p = mapper.readValue(exchange.getRequestBody(), Product.class);
            repo.save(p);
            sendResponse(exchange, 201, mapper.writeValueAsString(p));
        } catch (JsonProcessingException e){
            sendResponse(exchange, 400,"{\"error\":\"Invalid Json\"}" );
        }catch (RepositoryException e){
            sendResponse(exchange, 500,"{\"error\":\"Something went wrong\"}" );
        }
    }


    private void handleDelete(HttpExchange exchange) throws IOException{
       try {
           Map<String,String> params = parseQuery(exchange);
           if (!params.containsKey("productId")){
               sendResponse(exchange,400, "{\"error\":\"Missing product Id\"}");
               return;
           }
           Product p = repo.findById(Long.parseLong(params.get("productId")));
           if (p == null){
               sendResponse(exchange, 404,"{\"error\":\"Product not found\"}" );
               return;
           }
           repo.deleteById(p.getProductId());
           sendResponse(exchange, 200, "{\"message\":\"Deleted\"}");
       } catch (RepositoryException e){
           sendResponse(exchange, 500, "{\"error\":\"Something went wrong\"}");
       } catch (NumberFormatException e){
           sendResponse(exchange, 400, "{\"error\":\"Invalid id\"}");
       }

    }

    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            Map <String,String> params = parseQuery(exchange);
            if (!params.containsKey("productId")){
                sendResponse(exchange, 400, "{\"error\":\"Invalid id\"}");
                return;
            }
            if (params.containsKey("add")) {
                handleAdd(exchange, params);
                return;
            }
            if (params.containsKey("remove")) {
                handleRemove(exchange, params);
                return;
            }
            sendResponse(exchange,400,"{\"error\":\"Invalid parameter\"}");

        } catch (NumberFormatException e){
            sendResponse(exchange, 400, "{\"error\":\"Invalid parameter\"}");
        } catch (RepositoryException e){
            sendResponse(exchange, 500, "{\"error\":\"Something went wrong\"}");
        }
    }

    private void handleAdd(HttpExchange exchange, Map<String,String> params) throws IOException {
            long id = Long.parseLong(params.get("productId"));
            int add = Integer.parseInt(params.get("add"));
            repo.addQuantity(id,add);
            sendResponse(exchange, 200,"{\"message\":\"Quantity changed\"}" );
    }


    private void handleRemove(HttpExchange exchange, Map<String,String> params) throws IOException {
            long id = Long.parseLong(params.get("productId"));
            int remove = Integer.parseInt(params.get("remove"));
            repo.removeQuantity(id,remove);
            sendResponse(exchange, 200,"{\"message\":\"Quantity changed\"}" );
    }
}
