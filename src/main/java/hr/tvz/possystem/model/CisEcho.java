package hr.tvz.possystem.model;

public final class CisEcho {
    private static final String ECHO_XML = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:tns="http://www.apis-it.hr/fin/2012/types/f73">
          <soapenv:Body>
            <tns:EchoRequest>ping</tns:EchoRequest>
          </soapenv:Body>
        </soapenv:Envelope>
        """;

    public static String callEcho(String endpoint) throws Exception {
        var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(endpoint))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(ECHO_XML))
                .build();

        var client = java.net.http.HttpClient.newBuilder().build();
        var resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }
}

