package com.github.oliverlj.jsonapi.configuration.parameters;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gitlab.oliverlj.jsonapi.configuration.parameters.FilterParameters;

@AutoConfigureMockMvc
@SpringBootTest(classes = FilterParametersTest.MockApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class FilterParametersTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldParseFilterParameters() throws Exception {
        // Given
        MockHttpServletRequestBuilder request = get("/mock").param("filter[att1][EQ]", "value1").param("filter[att1][EQ]", "value2 ").param("filter[att1][EQ]", "")
                .param("filter[att2][EQ]", "value3");

        // When
        ResultActions result = this.mvc.perform(request);

        // Then
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters").isMap());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters.att1").exists());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters.att1.EQ").exists());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters.att1.EQ").value(containsInAnyOrder("value1", "value2")));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters.att2").exists());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters.att2.EQ").exists());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.filters.att2.EQ").value("value3"));
    }

    @SpringBootApplication
    public static class MockApplication {
        
        @Bean
        public MockController mockController() {
            return new MockController();
        }
    }

    @RestController
    @RequestMapping("mock")
    public static class MockController {

        @GetMapping
        public FilterParameters filter(FilterParameters parameters) {
            return parameters;
        }
    }

}
