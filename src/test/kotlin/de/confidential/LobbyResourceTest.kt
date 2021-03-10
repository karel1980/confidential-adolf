package de.confidential

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class LobbyResourceTest {

    @Test
    fun testHelloEndpoint() {
        given()
          .`when`().get("/lobby")
          .then()
             .statusCode(200)
             .body(`is`("Hello RESTEasy"))
    }

}