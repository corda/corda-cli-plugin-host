# Generate Group Policy

This is a sub-command under the `mgm` plugin for generating a GroupPolicy.json file.

Running `groupPolicy` without any command line arguments prints a sample GroupPolicy file for the user to manually tweak.
```shell
java -Dpf4j.pluginsDir=build/plugins -jar app/build/libs/corda-cli-0.0.1-beta.jar mgm groupPolicy
```

Alternatively, the following command line arguments can be used to define the static network section of the GroupPolicy:

| Argument            | Description                                                          |
|---------------------|----------------------------------------------------------------------|
| --file, -f          | Path to a JSON or YAML file that contains static network information |
| --name              | Member's X.500 name                                                  |
| --endpoint-url      | Endpoint base URL                                                    |
| --endpoint-protocol | Version of end-to-end authentication protocol                        |

To generate GroupPolicy using file input:
> Sample files are available [here](#sample-files).

```shell
java -Dpf4j.pluginsDir=build/plugins -jar app/build/libs/corda-cli-0.0.1-beta.jar mgm groupPolicy --file="app/build/resources/src.yaml"
```
Note:
1. Only one of `memberNames` and `members` blocks may be present.
2. Single endpoint is assumed for all members when `memberNames` is used.
3. Endpoint information specified under `members` overrides endpoint information set at the root level. An error is thrown if endpoint information is not provided at all.

To generate GroupPolicy using string parameters:
```shell
java -Dpf4j.pluginsDir=build/plugins -jar app/build/libs/corda-cli-0.0.1-beta.jar mgm groupPolicy --name="C=GB, L=London, O=Member1" --name="C=GB, L=London, O=Member2" --endpoint-protocol=5 --endpoint-url="http://dummy-url"
```
Note:
1. Passing one or more `--name` without specifying endpoint information will throw an error.
2. Not passing any `--name` will return a GroupPolicy with an empty list of static members.
3. Single endpoint is assumed for all members.

## Sample files

1. Sample JSON with `memberNames`
```json
{
  "endpointUrl": "http://dummy-url",
  "endpointProtocol": 5,
  "memberNames": ["C=GB, L=London, O=Member1", "C=GB, L=London, O=Member1"]
}
```

2. Sample JSON with `members`
```json
{
  "endpointUrl": "http://dummy-url",
  "endpointProtocol": 5,
  "members": [
    {
      "name": "C=GB, L=London, O=Member1",
      "status": "PENDING",
      "endpointProtocol": 10
    },
    {
      "name": "C=GB, L=London, O=Member2"
    }
  ]
}
```

3. Sample YAML with `memberNames`
```yaml
endpointUrl: "http://dummy-url"
endpointProtocol: 5
memberNames: ["C=GB, L=London, O=Member1", "C=GB, L=London, O=Member2"]
```

4. Sample YAML with `members`
```yaml
endpointUrl: "http://dummy-url"
endpointProtocol: 5
members:
    - name: "C=GB, L=London, O=Member1"
      status: "PENDING"
      endpointProtocol: 10
    - name: "C=GB, L=London, O=Member2"
```