#### **Overview**
This project uses Kubernetes ConfigMap to provide dynamic configuration to a Spring Boot application. The ConfigMap is mounted as a file inside the container, allowing the application to read structured YAML data at runtime. To avoid allowing this to overwrite undesirable application properties, we safeguard by specifying the specific subpath in the YAML file (`config.servers.*`) when declaring the data section of the ConfigMap.  

---

## **ConfigMap Definition**

The ConfigMap is defined in `helm/templates/configmap.yaml`:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Release.Name }}-config
data:
  servers.yaml: |
    config:
      servers:
        {{- toYaml .Values.config.servers | nindent 8 }}
```
- 
`toYaml` converts a **subsection of `values.yaml`** into valid YAML. This is placed under the desired YAML path (`config > servers > {}`) to ensure any properties loaded from values.yaml will not clobber existing properties nor can set anything outside of the scope of that file. This prevents someone from defining overrides (whether intentional or not).

---

## **Mounting the ConfigMap**

The deployment mounts the ConfigMap as a file in `/app/config/servers.yaml`:

```yaml
volumes:
  - name: config-volume
    configMap:
      name: {{ .Release.Name }}-config

containers:
  - name: mountyaml
    image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
    volumeMounts:
      - name: config-volume
        mountPath: "/app/config/servers.yaml"
        subPath: "servers.yaml"
```
- **`volumes`**: Attaches the ConfigMap as a volume.
- **`volumeMounts`**: Mounts the `servers.yaml` file inside the container.

---

## **How the Application Reads the ConfigMap**
Spring Boot is configured to read `servers.yaml` at startup via `application.yaml` using an import directive:

```yaml
spring:
  config:
    import: "optional:file:/app/config/servers.yaml"
```
(The `optional:` prefix ensures the app does not fail if the file is missing.)

`ServerConfigProperties` then binds the contents into a Java object in standard Spring fashion:
  ```java
  @Component
  @ConfigurationProperties(prefix = "config.servers")
  public class ServerConfigProperties { ... }
  ```

---

## **Usage**
### **Update Configuration**

Modify `values.yaml`:

```yaml
config:
  servers:
    somename: jdbc://some.url/
    someothername: jdbc://some.other.url/
```

Apply changes:

```sh
helm upgrade --install my-app ./helm
kubectl rollout restart deployment my-app
```

Verify:

```sh
kubectl exec -it <pod-name> -- cat /app/config/servers.yaml
```

Requested via curl:

```bash
curl localhost:8080/api/servers | jq .
```

Response:

```json
{
  "serverListings": [
    {
      "name": "DEPLOYOVER",
      "url": "jdbc://guest@deployover.net:8521/db"
    },
    {
      "name": "JDKENDALL",
      "url": "jdbc://admin@jdkendall.com:9931/fake"
    },
    {
      "name": "SOMEWHERE",
      "url": "jdbc://aslan@narnia:1212/wardrobe"
    }
  ]
}
```
