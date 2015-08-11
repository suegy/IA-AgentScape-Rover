
Rovers Coursework
=================

This zip file contains the starter code for the rovers coursework.

First, install Agentscape 2 Milestone 2.

Then, edit each of the for pom.xml files
rover/pom.xml
rover.shared/pom.xml
rover.service/pom.xml
rover.monitor/pom.xml

and change the line
<org.iids.aos.install.path>${user.home}/agents-coursework/agentscape</org.iids.aos.install.path>
to point to wherever you installed agentscape.

Then use maven to 'mvn install' to compile the code.  This will automatically install the jar files into the appropriate places in your agentscape directory.  Agents will go in agents/, the service will go in services/ etc.  You probably need to compile shared first, then service, and then the monitor and rover agents.

You should base your rover agent on the TestRover.  If you change the name of the class you will need to edit the pom.xml.

Currently there is only one default scenario available, and the monitor agent just outputs status of the rovers to the console.  There will be a GUI monitor agent soon, and this will allow you to select different scenarios.

Please ask any questions and report any bugs on moodle.

