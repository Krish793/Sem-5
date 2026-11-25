package com.kalasetu.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "kalasetu.mode=memory",
        "kalasetu.data-dir=target/test-runtime-data"
})
class WebIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEndToEndMarketplaceFlowAndErrorPaths() throws Exception {
        // 1. Actuator & System Module Status
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mvc.perform(get("/api/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules", hasSize(6)));

        mvc.perform(get("/api/auth/demo-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // 2. Demo Login + Invalid Login
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aarav.buyer@weavo.demo\",\"password\":\"Weavo@2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aarav.buyer@weavo.demo\",\"password\":\"WrongPass\"}"))
                .andExpect(status().isForbidden());

        // 3. Registration (Customer + Artisan + Admin block + Consent block)
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Kavya Joshi\",\"email\":\"kavya@test.in\",\"password\":\"Pass@123\",\"role\":\"CUSTOMER\",\"dpdpConsent\":false}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Fake Admin\",\"email\":\"hacker@test.in\",\"password\":\"Pass@123\",\"role\":\"ADMIN\",\"dpdpConsent\":true}"))
                .andExpect(status().isForbidden());

        MvcResult artisanReg = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Dinesh Vankar\",\"email\":\"dinesh.weaver@test.in\",\"password\":\"Pass@123\",\"role\":\"ARTISAN\",\"city\":\"Bhujodi\",\"workshopName\":\"Vankar Looms\",\"villageCluster\":\"Bhujodi\",\"craftSpecialty\":\"Kala Cotton\",\"dpdpConsent\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artisanProfile.kycStatus").value("PENDING"))
                .andReturn();

        JsonNode artJson = mapper.readTree(artisanReg.getResponse().getContentAsString());
        String newArtisanUserId = artJson.get("user").get("id").asText();
        String newArtisanProfileId = artJson.get("artisanProfile").get("id").asText();

        // Pending artisan cannot publish product yet (403)
        mvc.perform(post("/api/artisan/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artisanUserId\":\"" + newArtisanUserId + "\",\"categoryId\":\"11111111-1111-1111-1111-111111111101\",\"title\":\"Kala Cotton Shawl\",\"craftType\":\"Weaving\",\"originVillage\":\"Bhujodi\",\"giCertified\":true,\"description\":\"Organic cotton\",\"priceInr\":1950,\"stockQty\":5}"))
                .andExpect(status().isForbidden());

        // Admin verifies KYC -> Artisan publishes product + uploads image
        mvc.perform(patch("/api/admin/artisans/" + newArtisanProfileId + "/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kycStatus\":\"VERIFIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("VERIFIED"));

        MockMultipartFile imgFile = new MockMultipartFile("file", "shawl.png", "image/png", new byte[]{1, 2, 3, 4});
        mvc.perform(multipart("/api/artisan/upload-image").file(imgFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", containsString("shawl.png")));

        mvc.perform(post("/api/artisan/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"artisanUserId\":\"" + newArtisanUserId + "\",\"categoryId\":\"11111111-1111-1111-1111-111111111101\",\"title\":\"Bhujodi Kala Cotton Shawl\",\"craftType\":\"Weaving\",\"originVillage\":\"Bhujodi\",\"giCertified\":true,\"description\":\"Organic cotton\",\"priceInr\":1950,\"stockQty\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bhujodi Kala Cotton Shawl"));

        // 4. Categories, Catalog Search & Security Check (XSS/SQLi blocked with 400)
        mvc.perform(get("/api/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/products/search?q=Bandhani"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", containsString("Bandhani")));

        mvc.perform(get("/api/products/search?q=' OR '1'='1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));

        // 5. Add to Cart & Checkout Happy Path (Test Mode)
        String buyerId = "22222222-2222-2222-2222-222222222201";
        String bandhaniId = "44444444-4444-4444-4444-444444444401";

        mvc.perform(post("/api/cart/" + buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + bandhaniId + "\",\"quantity\":1}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/cart/" + buyerId)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        MvcResult orderRes = mvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + buyerId + "\",\"shippingAddress\":\"Navrangpura, Ahmedabad\",\"paymentMethodCode\":\"UPI_TEST\",\"paymentToken\":\"aarav@okicici\",\"simulatePaymentFailure\":false,\"dpdpConsentAccepted\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID_TEST"))
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"))
                .andReturn();

        JsonNode orderJson = mapper.readTree(orderRes.getResponse().getContentAsString());
        String orderId = orderJson.get("id").asText();
        String orderNumber = orderJson.get("orderNumber").asText();

        mvc.perform(get("/api/orders/customer/" + buyerId)).andExpect(status().isOk());
        mvc.perform(get("/api/orders")).andExpect(status().isOk());
        mvc.perform(get("/api/orders/" + orderNumber + "/receipt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(orderNumber)));

        // 6. Verified Review Success + Anti-Fake-Review Guard (403)
        mvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + bandhaniId + "\",\"customerId\":\"" + buyerId + "\",\"orderId\":\"" + orderId + "\",\"rating\":5,\"comment\":\"Genuine Bandhani craftsmanship!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedPurchase").value(true));

        mvc.perform(get("/api/products/" + bandhaniId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verifiedPurchase").value(true));

        mvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + bandhaniId + "\",\"customerId\":\"" + buyerId + "\",\"orderId\":\"99999999-9999-9999-9999-999999999999\",\"rating\":5,\"comment\":\"Fake review attempt\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED_ACTION"));

        // 7. Simulated Payment Failure (402) & Out of Stock (409)
        mvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + buyerId + "\",\"lines\":[{\"productId\":\"" + bandhaniId + "\",\"quantity\":1}],\"shippingAddress\":\"Navrangpura, Ahmedabad\",\"paymentMethodCode\":\"UPI_TEST\",\"paymentToken\":\"fail@upi\",\"simulatePaymentFailure\":true,\"dpdpConsentAccepted\":true}"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_FAILED"));

        mvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + buyerId + "\",\"lines\":[{\"productId\":\"" + bandhaniId + "\",\"quantity\":45}],\"shippingAddress\":\"Navrangpura, Ahmedabad\",\"paymentMethodCode\":\"UPI_TEST\",\"paymentToken\":\"aarav@okicici\",\"simulatePaymentFailure\":false,\"dpdpConsentAccepted\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("OUT_OF_STOCK"));

        // 8. Admin Concurrency Demo, Notifications, Audit & CSV Export
        mvc.perform(post("/api/admin/concurrency-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + bandhaniId + "\",\"customerId\":\"" + buyerId + "\",\"threads\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oversellPrevented").value(true));

        mvc.perform(get("/api/admin/artisans")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/notifications")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/audit-logs")).andExpect(status().isOk());
        mvc.perform(get("/api/files/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("WV-BN-001")));
    }
}
