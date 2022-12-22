# bidrag-dokument-bestilling
Tjeneste for å bestille redigerbar og ikke-redigerbare bidrag dokumenter

[![continuous integration](https://github.com/navikt/bidrag-dokument-bestilling/actions/workflows/ci.yaml/badge.svg)](https://github.com/navikt/bidrag-dokument-bestilling/actions/workflows/ci.yaml)
[![release bidrag-dokument-bestilling](https://github.com/navikt/bidrag-dokument-bestilling/actions/workflows/release.yaml/badge.svg)](https://github.com/navikt/bidrag-dokument-bestilling/actions/workflows/release.yaml)

## Beskrivelse

## Secrets
Applikasjonen bruker noen hemmeligheter som må settes opp før deploy til NAIS
```
kubectl create secret generic bidrag-dokument-bestilling-secrets \
  --from-literal=MQ_USER_USERNAME=username \
  --from-literal=MQ_USER_PASSWORD=pass \
  --from-literal=BREVSERVER_PASSORD=pass
```

### Live reload
Med `spring-boot-devtools` har Spring støtte for live-reload av applikasjon. Dette betyr i praksis at Spring vil automatisk restarte applikasjonen når en fil endres. Du vil derfor slippe å restarte applikasjonen hver gang du gjør endringer. Dette er forklart i [dokumentasjonen](https://docs.spring.io/spring-boot/docs/1.5.16.RELEASE/reference/html/using-boot-devtools.html#using-boot-devtools-restart).
For at dette skal fungere må det gjøres noe endringer i Intellij instillingene slik at Intellij automatisk re-bygger filene som er endret:

* Gå til `Preference -> Compiler -> check "Build project automatically"`
* Gå til `Preference -> Advanced settings -> check "Allow auto-make to start even if developed application is currently running"`

#### Kjøre lokalt mot nais tjenester
For å kunne kjøre lokalt mot sky må du gjøre følgende

Åpne terminal på root mappen til `bidrag-dokument-bestilling`
Konfigurer kubectl til å gå mot kluster `dev-gcp`
```bash
# Log inn til GPC
gcp auth login --update-adc
# Sett cluster til dev-fss
kubectx dev-gcp
# Sett namespace til bidrag
kubens bidrag 

# -- Eller hvis du ikke har kubectx/kubens installert 
# (da må -n=bidrag legges til etter exec i neste kommando)
kubectl config use dev-gcp
```
Deretter kjør følgende kommando for å importere secrets. Viktig at filen som opprettes ikke committes til git

```bash
kubectl exec --tty deployment/bidrag-dokument-bestilling-feature printenv | grep -E 'AZURE_APP_CLIENT_ID|AZURE_APP_CLIENT_SECRET|AZURE_OPENID_CONFIG_TOKEN_ENDPOINT|AZURE_APP_TENANT_ID|AZURE_APP_WELL_KNOWN_URL|KODEVERK_URL|BIDRAG_PERSON_URL|BIDRAG_DOKUMENT_URL|BIDRAG_ORGANISASJON_URL|BIDRAG_SAK_URL|SCOPE' > src/main/resources/application-lokal-nais-secrets.properties
```

Start opp applikasjonen ved å kjøre [BidragDokumentBestillingLokal.kt](src/test/kotlin/no/nav/bidrag/dokument/bestilling/BidragDokumentBestillingLokal.kt).

Deretter kan tokenet brukes til å logge inn på swagger-ui http://localhost:8999/swagger-ui.html