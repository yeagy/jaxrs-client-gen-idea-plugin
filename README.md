##IntelliJ IDEA plugin for generating JAX-RS resource source files

Install jaxrs-client-gen-idea-plugin.zip file manually from the settings plugin section.

####Usage
  - Right click on .java or .class file in the project view and select "Generate JAX-RS Client"
  
  Alternatively, to finding the resource in the project view, you can specify it manually.
  
  - Click "Tools" from the Menu and select "Generate JAX-RS Client"
  - Select the module the resource is located in.
  - Enter full classname of resource (i.e. com.foo.ExResource)

#####Notes
It is required to specify the module as the Resource class could exist in many modules, possibly with different versions and forms.

Output will be written to directory jaxrs-client-generated-source/ in the selected module.
