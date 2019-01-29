## Kylo

In order to get a Kylo sandbox environment, eihter a virtual machine can be downloaded from the Kylo website or the Kylo sandbox can be provisioned on AWS: <https://kylo.io/quickstart.html>.

### Change Keyboard to Swiss-German

```
yum install kbd

localectl set-keymap ch-de_mac
```

Add the IP address as `kylo` to the `/etc/hosts` file.

* Kylo UI: <http://kylo:8400> login: dladmin/thinkbig
* Kylo API: <http://kylo:8400/api-docs/index.html>
* Kylo NiFi: <http://kylo:8079/nifi/>
* API Doc: <http://kylo:8400/api-docs/index.html>
* Ambari UI: <http://kylo:8080> - login admin/admin


* Sandbox OS Login: root/kylo
