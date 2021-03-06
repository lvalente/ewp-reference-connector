package eu.erasmuswithoutpaper.discovery.boundary;

import eu.erasmuswithoutpaper.api.architecture.MultilineString;
import eu.erasmuswithoutpaper.api.architecture.StringWithOptionalLang;
import eu.erasmuswithoutpaper.api.discovery.Discovery;
import eu.erasmuswithoutpaper.api.discovery.Manifest;
import eu.erasmuswithoutpaper.api.echo.Echo;
import eu.erasmuswithoutpaper.api.institutions.Institutions;
import eu.erasmuswithoutpaper.api.ounits.OrganizationalUnits;
import eu.erasmuswithoutpaper.api.registry.ApisImplemented;
import eu.erasmuswithoutpaper.api.registry.Hei;
import eu.erasmuswithoutpaper.api.registry.OtherHeiId;
import eu.erasmuswithoutpaper.common.control.EwpKeyStore;
import eu.erasmuswithoutpaper.common.control.GlobalProperties;
import eu.erasmuswithoutpaper.organization.entity.Institution;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Stateless
@Path("")
public class ManifestResource {
    @PersistenceContext(unitName = "connector")
    EntityManager em;

    @Inject
    GlobalProperties globalProperties;
    
    @Inject
    EwpKeyStore keystoreController;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("manifest-old")
    @Produces(MediaType.APPLICATION_XML)
    public String manifestOld() throws URISyntaxException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource("manifest.xml")
                .toURI())));
        return content;
    }
    
    @GET
    @Path("manifest")
    @Produces(MediaType.APPLICATION_XML)
    public javax.ws.rs.core.Response manifest() {
        Manifest manifest = new Manifest();
        
        ApisImplemented apisImplemented = new ApisImplemented();
        apisImplemented.getAny().add(getDiscoveryEntry());
        apisImplemented.getAny().add(getEchoEntry());
        apisImplemented.getAny().add(getInstitutionsEntry());
        apisImplemented.getAny().add(getOrganizationalUnitsEntry());
        manifest.setApisImplemented(apisImplemented);
        
        manifest.setInstitutionsCovered(getInstitutionsCovered());
        manifest.setClientCredentialsInUse(getClientCredentialsInUse());
        manifest.setAdminNotes(getAdminNotes());

        return javax.ws.rs.core.Response.ok(manifest).build();
    }
    
    private Manifest.InstitutionsCovered getInstitutionsCovered() {
        Manifest.InstitutionsCovered institutionsCovered = new Manifest.InstitutionsCovered();
        List<Institution> institutionList = em.createNamedQuery(Institution.findAll).getResultList();
        List<Hei> heis = institutionList.stream().map((institution) -> createHei(institution)).collect(Collectors.toList());
        institutionsCovered.getHei().addAll(heis);
        
        return institutionsCovered;
    }

    private Manifest.ClientCredentialsInUse getClientCredentialsInUse() {
        Manifest.ClientCredentialsInUse clientCredentialsInUse = null;
        
        String certificate = keystoreController.getCertificate();
        if (certificate != null) {
            clientCredentialsInUse = new Manifest.ClientCredentialsInUse();
            clientCredentialsInUse.getCertificate().add(certificate);
        }
        
        return clientCredentialsInUse;
    }
    
    private Discovery getDiscoveryEntry() {
        Discovery discovery = new Discovery();
        discovery.setVersion("4.0.1");
        discovery.setUrl(getBaseUri() + "manifest");
        return discovery;
    }
    
    private Echo getEchoEntry() {
        Echo echo = new Echo();
        echo.setVersion("1.0.1");
        echo.setUrl(getBaseUri() + "echo");
        return echo;
    }
    
    private Institutions getInstitutionsEntry() {
        Institutions institutions = new Institutions();
        institutions.setVersion("0.3.0");
        institutions.setUrl(getBaseUri() + "institutions");
        institutions.setMaxHeiIds(BigInteger.valueOf(globalProperties.getMaxInstitutionsIds()));
        return institutions;
    }

    private OrganizationalUnits getOrganizationalUnitsEntry() {
        OrganizationalUnits organizationalUnits = new OrganizationalUnits();
        organizationalUnits.setVersion("0.1.0");
        organizationalUnits.setUrl(getBaseUri() + "ounits");
        organizationalUnits.setMaxOunitIds(BigInteger.valueOf(globalProperties.getMaxOunitsIds()));
        return organizationalUnits;
    }
    
    private Hei createHei(Institution institution) {
        Hei hei = new Hei();
        hei.setId(institution.getInstitutionId());

        institution.getOtherId().stream().map((otherId) -> {
            OtherHeiId oid = new OtherHeiId();
            oid.setType(otherId.getIdType());
            oid.setValue(otherId.getIdValue());
            return oid;
        }).forEachOrdered((oid) -> {
            hei.getOtherId().add(oid);
        });
        
        institution.getName().stream().map((name) -> {
            StringWithOptionalLang swolName = new StringWithOptionalLang();
            swolName.setLang(name.getLang());
            swolName.setValue(name.getText());
            return swolName;
        }).forEachOrdered((name) -> {
            hei.getName().add(name);
        });
        return hei;
    }

    private MultilineString getAdminNotes() {
        MultilineString multilineString = new MultilineString();
        multilineString.setValue("This is a EWP reference connector instance.");
        return multilineString;
    }
    
    private String getBaseUri() {
        Optional<String> baseUriProperty = globalProperties.getBaseUri();
        return baseUriProperty.isPresent() ? baseUriProperty.get() + "/rest/" : uriInfo.getBaseUri().toString();
    }
}
