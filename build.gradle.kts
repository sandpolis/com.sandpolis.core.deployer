//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//

plugins {
	id("java-library")
	id("com.sandpolis.build.module") version "+"
	id("com.sandpolis.build.protobuf") version "+"
	id("com.sandpolis.build.publish") version "+"
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.1")

	if (project.getParent() == null) {
		implementation("com.sandpolis:core.net:+")
		implementation("com.sandpolis:core.instance:+")
	} else {
		implementation(project(":module:com.sandpolis.core.net"))
		implementation(project(":module:com.sandpolis.core.instance"))
	}
}
