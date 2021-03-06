
# UroSound #

![Status](https://img.shields.io/badge/Version-Experimental-brightgreen.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Introduction
------------
UroSound is a platform that performs non-intrusive sound-based uroflowmetry with a smartwatch.


## Table Of Contents ##

0. [Folder Structure](##Folder%20Structure)
1. [UroSound App](##UroSound%20App)
2. [Lists of compatible smartwatches](#List%20of%20compatible%20smartwatches)
3. [Acknowledgement](#acknowledgement)
4. [Support](#support)
5. [Publication](#publication)

##  Folder Structure ## 
-------------
- `audios_dataset`: voiding events audio files recorded with the UroSound App and the Oppo smartwatch, for 13 volunteer patients
- `standard_uroflowmetries`: standard uroflowmetry test results for 8 volunteer patients
- `UroSound App`: App source code for Wear OS devices.

Note: to preserve the privacy of the volunteer patients, they have been idenficied with an ID. The letter corresponds to different clinics.



## UroSound App ##
--------------
Currently only available for Android Wear OS. It is available for Spanish and English languages.

*Prerequisites:

- Latest Android Studio (The project has been built with version 4.2.1 )
- Android SDK 28
- Android Build Tools v30.0.3
- Instructions manual: comming soon..

- `UroSound App screenshots`:
--------------
![UroSound GUI](images/UrosoundGUI_english.png?raw=true "Title")


## List of compatible  smartwatches ##
--------------
- Oppo Watch (this one was used to record the dataset audios)
- Ticwatch E
- Fossil Gen 5E
- iWatch S5

## Support ##
--------------
- Developed with Laura Arjona, Alfonso Bahillo, and Luis E. Diez @DeustoTech https://deustotech.deusto.es/
- Collaborator: Dr. Antón Arruza Echevarria, University Hospital of Cruces, Cruces, Spain.
- Contact [Laura Arjona](https://www.linkedin.com/in/laura-arjona-3b687534/?locale=en_US) @DeustoTech through email `laura.arjona` at deusto.es
- Contact [Enrique Diez](https://deustotech.deusto.es/member/luis-enrique-diez/) @DeustoTech through email `luis.enrique.diez` at deusto.es


## Acknowledgement ##
--------------
This research was supported by the Spanish Ministry of Science and Innovation under the PeaceOfMind project (ref. PID2019-105470RB-C31).

Thanks to all the volunteer patients that made this project possible


## Publication ##
--------------
L. Arjona, L. Enrique Diez, A. Bahillo Martinez and A. Arruza Echevarria, 
"UroSound: A Smartwatch-based Platform to Perform Non-Intrusive Sound-based Uroflowmetry,
" in IEEE Journal of Biomedical and Health Informatics, doi: 10.1109/JBHI.2022.3140590.