##IntelliJ IDEA plugin for generating JAX-RS resource source files

Install jaxrs-client-gen-idea-plugin.zip file manually from the settings plugin section.

####Usage: 
  - Click "Tools" from the Menu and select "Generate JAX-RS Client"
  - Select the module the resource is located in.
  - Enter full classname of resource (i.e. com.foo.ExResource)
  - Optionally select asynchronous clients.
  
It is required to specify the module as the Resource class could exist in many modules, possibly with different versions and forms.

Output will be written to directory jaxrs-client-generated-source/ in the selected module.

---
I'd like this plugin to function off right-clicking the file desired and adding a action to the context menu. Alas, IDEA documentation blows. Maybe next time.