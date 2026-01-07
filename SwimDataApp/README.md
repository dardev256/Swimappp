# Swim Data App

The Swim Data App is a JavaFX-based desktop application designed to empower coaches, swimmers, and parents by providing a centralized platform for tracking and analyzing swimming performance data. It replaces scattered spreadsheets with a modern dashboard for visualizing IMX scores, test sets, and competitive comparisons.

## Table of Contents
* [Features](#features)
* [Technologies Used](#technologies-used)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Usage & Running](#usage--running)
* [Data Format](#data-format)

## Features

* **Swimmer Dashboard:** A centralized hub displaying high-salience data including Age, Roster Group, IMX Score, and Personal Bests.
* **IMX Analysis:** Visualize IMX score progression over time and view detailed breakdowns of points per event.
* **Comparison Tool:** Compare two swimmers side-by-side to analyze head-to-head performance and historical trends.
* **Test Set Analysis:** specialized views for coaches to track performance on specific training sets (e.g., Distance Swim, Sprint Test).
* **Excel Integration:** Import data directly from standardized Excel spreadsheets.

## Technologies Used

* **Java:** JDK 17
* **Framework:** JavaFX 21.0.2
* **Build Tool:** Maven
* **Data Processing:**
    * **Apache POI (5.2.3):** For reading and parsing Excel (.xlsx) files.
    * **Jsoup (1.15.3):** For HTML parsing (if applicable).

## Prerequisites

Ensure you have the following installed on your local machine:

1.  **Java Development Kit (JDK) 17**
2.  **Maven** (Apache Maven 3.6.0 or higher recommended)

## Installation

1.  **Clone the repository** (or unzip the project source):
    ```bash
    git clone <your-repository-url>
    cd SwimDataApp
    ```

2.  **Build the project** using Maven to download dependencies (JavaFX, POI, etc.):
    ```bash
    mvn clean install
    ```

## Usage & Running

### 1. Launch the Application
You can run the application directly from the command line using the Maven JavaFX plugin configured in the `pom.xml`:

```bash
mvn javafx:run

There is a sample file with data to test the app in the sample folder.
