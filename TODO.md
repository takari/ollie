Right now a JacksonModule is present that overrides the ObjectMapper provider in Siesta specifically to create an ObjectMapper that doesn't emit nulls, but I think we need to tweak Siesta to allow a mapping of specific serializers for a class[1] or allow the customization of the ObjectMapper before any serializing is performed. Swagger provides its own implementation of MessageBodyWriter for its entities and they mesh what they do there with what the Swagger UI expects. If we embed Swagger with services we need to be able to use what the authors provide and we might need something different so one global ObjectMapper won't work.

For now, we just have an ObjectMapper that works better with Swagger but we should use exactly what comes from the Swagger JAXRS project.

# Integrations
- https://github.com/opentracing-contrib/java-jaxrs
- swagger
- configuration management

[1]: https://stackoverflow.com/questions/24489186/customize-json-serialization-with-jaxrs
