package emcer.cg.fr.gestionutilisateuronline.power;

import com.fasterxml.jackson.databind.ObjectMapper;
import emcer.cg.fr.gestionutilisateuronline.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Power API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class PowerControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
//        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/auth/login").header(HttpHeaders.AUTHORIZATION,
//                "Basic " + Base64Utils.encodeToString("john:123456".getBytes())));
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("massire", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token"); // Don't forget to add "Bearer " as prefix.
    }

    @Test
    @DisplayName("Check findAllArtifacts (GET)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
        // Reset H2 database before calling this test case.
    void testFindAllArtifactsSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/pouvoirs").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(6)));
    }

    @Test
    @DisplayName("Check findArtifactById (GET)")
    void testFindArtifactByIdSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/pouvoirs/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Deluminator"));
    }

    @Test
    @DisplayName("Check findArtifactById with non-existent id (GET)")
    void testFindArtifactByIdNotFound() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/pouvoirs/10").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Power not found with id:10"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    /*@Test
    @DisplayName("Check addArtifact with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddArtifactSuccess() throws Exception {
        Power a = new Power();
        a.setName("Remembrall");
        a.setDescription("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered.");
        a.setImageUrl("ImageUrl");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(post(this.baseUrl + "/powers").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("Remembrall"))
                .andExpect(jsonPath("$.data.description").value("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered."))
                .andExpect(jsonPath("$.data.imageUrl").value("ImageUrl"));
        this.mockMvc.perform(get(this.baseUrl + "/powers").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));
    }*/

    @Test
    @DisplayName("Check addArtifact with invalid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddArtifactErrorWithInvalidInput() throws Exception {
        Power a = new Power();
        a.setName(""); // Name is not provided.
        a.setDescription(""); // Description is not provided.
        a.setImageUrl(""); // ImageUrl is not provided.

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(post(this.baseUrl + "/pouvoirs").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided Arguments are invalid, see data for details. "))
                .andExpect(jsonPath("$.data.name").value("name is required."))
                .andExpect(jsonPath("$.data.description").value("description is required."))
                .andExpect(jsonPath("$.data.imageUrl").value("imageUrl is required."));
        this.mockMvc.perform(get(this.baseUrl + "/pouvoirs").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(6)));
    }


    @Test
    @DisplayName("Check updateArtifact with valid input (PUT)")
    void testUpdateArtifactSuccess() throws Exception {
        Power a = new Power();
        a.setId(2L);
        a.setName("Updated artifact name");
        a.setDescription("Updated description");
        a.setImageUrl("Updated imageUrl");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(put(this.baseUrl + "/pouvoirs/2").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.name").value("Updated artifact name"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.imageUrl").value("Updated imageUrl"));
    }

    @Test
    @DisplayName("Check updateArtifact with non-existent id (PUT)")
    void testUpdateArtifactErrorWithNonExistentId() throws Exception {
        Power a = new Power();
        a.setId(10L); // This id does not exist in the database.
        a.setName("Updated artifact name");
        a.setDescription("Updated description");
        a.setImageUrl("Updated imageUrl");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(put(this.baseUrl + "/pouvoirs/10").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Power with id:10"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check updateArtifact with invalid input (PUT)")
    void testUpdateArtifactErrorWithInvalidInput() throws Exception {
        Power a = new Power();
        a.setId(1L); // Valid id
        a.setName(""); // Updated name is empty.
        a.setDescription(""); // Updated description is empty.
        a.setImageUrl(""); // Updated imageUrl is empty.

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(put(this.baseUrl + "/pouvoirs/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided Arguments are invalid, see data for details. "))
                .andExpect(jsonPath("$.data.name").value("name is required."))
                .andExpect(jsonPath("$.data.description").value("description is required."))
                .andExpect(jsonPath("$.data.imageUrl").value("imageUrl is required."));
        this.mockMvc.perform(get(this.baseUrl + "/pouvoirs/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Deluminator"));
    }

    @Test
    @DisplayName("Check deleteArtifact with valid input (DELETE)")
    void testDeleteArtifactSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/pouvoirs/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Power deleted"));
        this.mockMvc.perform(get(this.baseUrl + "/pouvoirs/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Power not found with id:1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteArtifact with non-existent id (DELETE)")
    void testDeleteArtifactErrorWithNonExistentId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/pouvoirs/10").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Power not found with id:10"))
                .andExpect(jsonPath("$.data").isEmpty());
    }


   /* @Test
    void testFindArtifactsByDescription() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "Hogwarts");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "2");
        requestParams.add("sort", "name,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/powers/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    void testFindArtifactsByNameAndDescription() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("name", "Sword");
        searchCriteria.put("description", "Hogwarts");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "2");
        requestParams.add("sort", "name,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/powers/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }*/

}