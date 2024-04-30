package quarkus.account;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountResourceTest {

  @Test
  @Order(1)
  void testRetrieveAll() {
    Response response =
        given()
            .when()
            .get("/accounts")
            .then()
            .statusCode(200)
            .body(
                containsString("George Baird"),
                containsString("Mary Taylor"),
                containsString("Diana Rigg"))
            .extract()
            .response();

    List<Account> accounts = response.jsonPath().getList("$");
    assertThat(accounts, not(empty()));
    assertThat(accounts, hasSize(3));
  }

  @Test
  @Order(2)
  void testRetrieveOneAccount() {
    Account account =
        given()
            .when()
            .get("/accounts/{accountNumber}", 123456789L)
            .then()
            .statusCode(200)
            .extract()
            .as(Account.class);

    assertThat(account.getAccountNumber(), equalTo(123456789L));
    assertThat(account.getCustomerName(), equalTo("George Baird"));
    assertThat(account.getBalance().toString(), equalTo("354.23"));
    assertThat(account.getAccountStatus(), equalTo(AccountStatus.OPEN));
  }

  @Test
  @Order(3)
  void testRetrieveInexistentAccount() {
    final String errorMessage =
        "an illegal argument was provided: 404: Account with id of 123 does not exist";
    given()
        .when()
        .get("/accounts/{accountNumber}", 123L) // inexistent
        .then()
        .statusCode(404)
        .body(containsString(errorMessage));
  }

  @Test
  @Order(4)
  void testCreateAccount() {
    Account account = new Account(324324L, 112244L, "Sandy Holmes", new BigDecimal("154.55"));
    given()
        .contentType(ContentType.JSON)
        .body(account)
        .when()
        .post("/accounts")
        .then()
        .statusCode(201);

    Response response = given().when().get("/accounts").then().statusCode(200).extract().response();

    List<Account> accounts = response.jsonPath().getList("$");
    assertThat(accounts, not(empty()));
    assertThat(accounts, hasSize(4));
  }

  @Test
  @Order(5)
  void testUpdateAccount() {
    Account account = new Account(324324L, 112244L, "Sandy Holmes", new BigDecimal("2500.00"));
    given()
        .contentType(ContentType.JSON)
        .body(account)
        .when()
        .put("/accounts")
        .then()
        .statusCode(200);

    Account found =
        given()
            .when()
            .get("/accounts/{accountNumber}", 324324L)
            .then()
            .statusCode(200)
            .extract()
            .as(Account.class);

    assertThat(found.getBalance().toString(), equalTo("2500.00"));
  }

  @Test
  @Order(6)
  void testDeleteAccount() {
    given().when().delete("/accounts/{accountNumber}", 324324L).then().statusCode(204);
    given().when().get("/accounts/{accountNumber}", 324324L).then().statusCode(404);
  }
}
