AGENT_NAME=conditionals

ROSIE_CONFIG=agent/rosie.${AGENT_NAME}.config

all: world ${ROSIE_CONFIG}

run_mobile_sim: ${AGENT_NAME}.world
	java soargroup.mobilesim.MobileGUI -w ${AGENT_NAME}.world

run_test: waypoint-map.soar internal-world.soar ${ROSIE_CONFIG} 
	python3 -m rosie.testing ${AGENT_NAME} -v
	echo "########### DIFF ##########"
	diff test-output.txt correct-output.txt

# agent/rosie.conditionals.config: Configures the rosie soar agent that will be sourced to run
${ROSIE_CONFIG}: ${AGENT_NAME}.config 
	java edu.umich.rosie.tools.config.RosieAgentConfigurator ${AGENT_NAME}.config


#### Generating World/Map Files

world: ${AGENT_NAME}.world waypoint-map.soar internal-world.soar

# ${AGENT_NAME}.world: Creates the world file that the mobile simulator will use
${AGENT_NAME}.world: world.info
	python3 -m rosie.tools.mapmaker world.info --world --output-dir . --agent-name ${AGENT_NAME}

# The internal world used during simulation
internal-world.soar: world.info
	python3 -m rosie.tools.mapmaker world.info --internal --output-dir .

# The connected waypoint map
waypoint-map.soar: world.info
	python3 -m rosie.tools.mapmaker world.info --map --output-dir .


#### Clean all files

clean:
	rm -rf agent
	rm -rf __pycache__



