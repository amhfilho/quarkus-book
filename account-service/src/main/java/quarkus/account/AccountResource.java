package quarkus.account;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Path("/accounts")
public class AccountResource {
  Set<Account> accounts = new HashSet<>();

  @GET
  public Set<Account> allAccounts() {
    return accounts;
  }

  @GET
  @Path("/{accountNumber}")
  public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
    Optional<Account> response =
        accounts.stream().filter(acct -> acct.getAccountNumber().equals(accountNumber)).findFirst();
    return response.orElseThrow(
        () ->
            new WebApplicationException(
                "Account with id of " + accountNumber + " does not exist", 404));
  }

  @POST
  public Response createAccount(Account account, @Context UriInfo uriInfo) {
    accounts.add(account);
    UriBuilder uriBuilder = uriInfo
            .getAbsolutePathBuilder()
            .path(Long.toString(account.accountNumber));
    return Response.created(uriBuilder.build()).type(MediaType.APPLICATION_JSON).build();
  }

  @PUT
  public Response updateAccount(Account account) {
    getAccount(account.getAccountNumber());
    accounts.removeIf(acct -> acct.getAccountNumber().equals(account.accountNumber));
    accounts.add(account);
    return Response.ok(account).build();

  }

  @DELETE
  @Path("/{accountNumber}")
  public Response deleteAccount(@PathParam("accountNumber") Long accountNumber) {
    accounts.removeIf(acct -> acct.getAccountNumber().equals(accountNumber));
    return Response.noContent().build();
  }

  @PostConstruct
  public void setup() {
    accounts.add(new Account(123456789L, 987654321L, "George Baird", new BigDecimal("354.23")));
    accounts.add(new Account(121212121L, 888777666L, "Mary Taylor", new BigDecimal("560.03")));
    accounts.add(new Account(545454545L, 222444999L, "Diana Rigg", new BigDecimal("422.00")));
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      String message = String.valueOf(code);
      if (exception.getMessage() != null) {
        message += ": " + exception.getMessage();
      }
      return Response.status(code)
              .entity(build(message))
              .type(MediaType.APPLICATION_JSON)
              .build();
    }

    private RestErrorResponse build(String message) {
      return new RestErrorResponse(null, "an illegal argument was provided: " + message);
    }
  }
}
