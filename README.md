<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]



<h1 align="center">Tournament Generator</h1>
  <p align="center">
    The Software for our PM3 Project
    <br />


<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

This project is a tournament management tool, used to create, edit and play tournaments. The tool is aimed useful in both 
professional and amateur settings and offers options for configurations to adapt it to the users needs. It offers user management
, so the same user can load their saved projects and play them again. Tournaments can be played in different modes, e.g. Single Elimination, 
Double Elimination etc. The participants are added by the user and consequently assembled in teams, after which a tournament tree
is generated. Then, teams can be moved around the tree to change matches to be played. Once the tree fits the users needs, 
the tournament can be started. During this phase, the tool is used to keep track of scores and the progression of the tree and 
the individual trees. Once the last round is played, the winning teams are displayed and the tournament is saved.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

This section should list any major frameworks/libraries used to bootstrap your project. Leave any add-ons/plugins for the acknowledgements section. Here are a few examples.

* [![Gradle][Gradle.js]][Gradle-url]
* [![JavaFX][JavaFX]][JavaFX-url]
* [GSON]
* [Java-Mail]


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

### Prerequisites
- Java 21
- IntelliJ IDEA (recommended)

### Installation

1. Clone the repo
   ```sh
   git clone https://github.zhaw.ch/PM3-Gruppe2-TurnierGenerator/TournamentGenerator.git
   ```
   
2. Download the latest version of JavaFX from [here](https://gluonhq.com/products/javafx/)
3. Extract the downloaded file and get the path to the lib folder. 
4. add the path to the lib folder to the VM options in the run configuration of the project in IntelliJ IDEA
   ```sh
   --module-path "C:\pathToYourFolder\javafx-sdk-23.0.1\lib" --add-modules=javafx.base,javafx.controls,javafx.graphics,javafx.media,javafx.fxml
    ```
5. Run the project

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

The application is intended to be used by tournament organizers to generate tournaments for various games. The application should be user-friendly and easy to use.


<!-- ROADMAP -->
## Roadmap

- [x] Finish Prototype
- [ ] Implement more games
- [ ] Add more Tournament types
- [ ] Add more features
- [ ] Make a mobile version
- [ ] Add more languages
- [ ] ...


<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=for-the-badge
[contributors-url]: https://github.zhaw.ch/PM3-Gruppe2-TurnierGenerator/TournamentGenerator/graphs/contributors
[issues-shield]: https://img.shields.io/github/issues/othneildrew/Best-README-Template.svg?style=for-the-badge
[issues-url]: https://github.zhaw.ch/PM3-Gruppe2-TurnierGenerator/TournamentGenerator/issues
[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=for-the-badge
[license-url]: https://github.zhaw.ch/PM3-Gruppe2-TurnierGenerator/TournamentGenerator/blob/dev/LICENSE
[product-screenshot]: images/screenshot.png
[Gradle.js]: https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white
[Gradle-url]: https://gradle.org/
[JavaFX]: https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white
[JavaFX-url]: https://openjfx.io/
[GSON]:https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.5/com/google/gson/Gson.html
[Java-Mail]: https://javaee.github.io/javamail/
