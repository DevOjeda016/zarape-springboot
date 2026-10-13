package org.utl.dsm.elzarape;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String user, String pass) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(user, pass)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    @Test
    void catalogIsPublicButWritesNeedAdmin() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"X\",\"type\":\"FOOD\"}")).andExpect(status().isUnauthorized());

        // un cliente registrado no puede escribir en el catálogo
        String signup = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana1\",\"password\":\"secreto1\",\"name\":\"Ana\",\"lastName\":\"Lopez\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String clientToken = json.readTree(signup).get("token").asText();
        mvc.perform(post("/api/categories").header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"X\",\"type\":\"FOOD\"}"))
                .andExpect(status().isForbidden());

        String admin = login("admin", "admin123");
        mvc.perform(post("/api/categories").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Postres\",\"type\":\"FOOD\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"mal\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void orderLifecycleUsesServerPricesAndEnforcesTransitions() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"luis1\",\"password\":\"secreto1\",\"name\":\"Luis\",\"lastName\":\"Perez\"}"))
                .andExpect(status().isCreated());
        String client = login("luis1", "secreto1");
        String kitchen = login("cocina", "cocina123");

        JsonNode product = json.readTree(mvc.perform(get("/api/products")).andReturn().getResponse().getContentAsString()).get(0);
        JsonNode branch = json.readTree(mvc.perform(get("/api/branches")).andReturn().getResponse().getContentAsString()).get(0);
        double price = product.get("price").asDouble();

        String created = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"branchId\":%d,\"paymentMethod\":\"CARD\",\"items\":[{\"productId\":%d,\"quantity\":2},{\"productId\":%d,\"quantity\":1}]}"
                                .formatted(branch.get("id").asLong(), product.get("id").asLong(), product.get("id").asLong())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_SERVICE"))
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        JsonNode order = json.readTree(created);
        org.junit.jupiter.api.Assertions.assertEquals(price * 3, order.get("total").asDouble(), 0.001);
        long id = order.get("id").asLong();

        // el cliente no ve la cocina
        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + client)).andExpect(status().isForbidden());
        mvc.perform(get("/api/orders/mine").header("Authorization", "Bearer " + client))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));

        // IN_SERVICE -> DELIVERED no es válido; IN_SERVICE -> READY -> DELIVERED sí
        patch(kitchen, id, "DELIVERED").andExpect(status().isConflict());
        patch(kitchen, id, "READY").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("READY"));
        patch(kitchen, id, "DELIVERED").andExpect(status().isOk());
        patch(kitchen, id, "CANCELLED").andExpect(status().isConflict());
    }

    @Test
    void rejectsEmptyOrderAndUnknownProduct() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"marta1\",\"password\":\"secreto1\",\"name\":\"Marta\",\"lastName\":\"Gil\"}"))
                .andExpect(status().isCreated());
        String client = login("marta1", "secreto1");
        mvc.perform(post("/api/orders").header("Authorization", "Bearer " + client).contentType(MediaType.APPLICATION_JSON)
                .content("{\"branchId\":1,\"paymentMethod\":\"CASH\",\"items\":[]}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/orders").header("Authorization", "Bearer " + client).contentType(MediaType.APPLICATION_JSON)
                .content("{\"branchId\":1,\"paymentMethod\":\"CASH\",\"items\":[{\"productId\":99999,\"quantity\":1}]}"))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions patch(String token, long id, String status) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/orders/" + id + "/status")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}"));
    }
}
