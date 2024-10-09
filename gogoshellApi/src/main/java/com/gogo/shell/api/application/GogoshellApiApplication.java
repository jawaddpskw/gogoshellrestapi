package com.gogo.shell.api.application;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

/**
 * @author Hp
 */
@Component(
	property = {
		JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/operations",
		JaxrsWhiteboardConstants.JAX_RS_NAME + "=Operations.Rest"
	},
	service = Application.class
)
public class GogoshellApiApplication extends Application {

	public Set<Object> getSingletons() {
		return Collections.<Object>singleton(this);
	}
	@GET
	@Path("/health")
	@Produces("text/plain")
	public String serviceIsUp() {
		return "Service is Up";
	}

	@GET
	@Path("/gogo/{command}")
	public Response health(@NotNull @PathParam("command") String command) {
		try {
			String lowerInput = command.toLowerCase();
			if (allowedGogoShellInputs.contains(lowerInput) || lowerInput.matches("b\\s+\\d+")) {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream printOut = new PrintStream(outStream);

				CommandSession session = commandProcessor.createSession(
						new ByteArrayInputStream(new byte[0]),
						printOut,
						System.err
				);
				session.execute(command);
				String output = outStream.toString();
				session.close();
				return Response.ok(output).build();
			} else {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("Error: Command not allowed")
						.build();
			}
		} catch (Exception e) {
			log.error(e);
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error: " + e.getMessage() + " Cause: " + cause)
					.build();
		}
	}
	private static final List<String> allowedGogoShellInputs = Arrays.asList("dm wtf", "diag", "b");

	@Reference
	CommandProcessor commandProcessor;
	Log log = LogFactoryUtil.getLog(GogoshellApiApplication.class);

}