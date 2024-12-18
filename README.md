## eSCLKt: AirScan (eSCL) in Kotlin

This project is a easy-to-use Kotlin library for using network-attached scanners supporting the eSCL/AirScan protocol.
This protocol allows for driverless usage of network scanners and is supported by most modern scanning devices.

The implementation is based on the eSCL specification as provided on https://mopria.org/spec-download and on practical
testing with actual network-attached scanners. The spec is very fuzzy in multiple places but the goal is to be
compatible with most scanners and scanner features.

## Usage as dependency

### Gradle

Add the following in your dependencies:

```
dependencies {
    implementation("io.github.chrisimx:esclkt:1.2.0")
}
```

### Maven

Add the following in your pom.xml as dependency:

```
<dependencies>
    ...
    <dependency>
        <groupId>io.github.chrisimx</groupId>
        <artifactId>esclkt</artifactId>
        <version>1.2.0</version>
    </dependency>
    ...
</dependencies>
```

## Building

Gradle is used as the build system. To compile the project, run the following command:

```./gradlew build```

To output a JAR file, run the following command:

```./gradlew jar```

## Bugs & feature requests

You can ask questions, report bugs or submit feature requests on the GitHub issue tracker. Please provide a detailed
description of the problem and if possible, network logs, application logs/stacktraces and your ScannerCapabilities.xml
in case of a bug.

## Contributions

Contributions are welcome, and it would be amazing if you want to help. Refer to
the Contribution Guidelines for more information.

## License

Copyright (C) 2024 Christian Nagel and contributors.

eSCLKt is free software: you can redistribute it and/or modify it under the terms of
the GNU General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

eSCLKt is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with eSCLKt.
If not, see <https://www.gnu.org/licenses/>.

SPDX-License-Identifier: GPL-3.0-or-later
