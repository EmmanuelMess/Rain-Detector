# Rain Detector

## v1.2.0

### Requirements

An application that shows a notification when it will rain in the short term future (5 day window),
using the information provided by the SMN (Servicio Meteorologico Nacional).
On first start the app should ask for location from available cities.
It should inform the rain probability and the day (of the week), and if there is a 0% probability,
it should say that there is no rain in the near future.
The notification should update 3 times a day, and not during the night, the notification should not
be dismissible or disappear.
If an error occurs or the API is down, a correctly localized error should be shown.

### Items that could change (as a probability)

#### High

* API SMN
* API Android
* Available cities for forecast
* Errors (number and type)

#### Medium

* Language
* Data provider

#### Low
* Underlying OS
* UI app
* UI notification
* Processing the probabilities


## v1.1.0

### Requirements

An application that shows a notification when it will rain in the short term future (5 day window),
using the information provided by the SMN (Servicio Meteorologico Nacional).
On first start the app should ask for location from available cities.
It should inform the rain probability and the day (of the week), and if there is a 0% probability,
it should say that there is no rain in the near future.
The notification should update 3 times a day, and not during the night, the notification should not
be dismissible or disappear.

### Items that could change (as a probability)

#### High

* API SMN
* API Android
* Available cities for forecast

#### Medium

* Language
* Data provider

#### Low
* Underlying OS
* UI app
* UI notification
* Processing the probabilities

## v1.0.0

### Requirements

An application that shows a notification when it will rain in the short term future (5 day window), 
using the information provided by the SMN (Servicio Meteorologico Nacional).
It should inform the rain probability and the day (of the week), and if there is a 0% probability, 
it should say that there is no rain in the near future.

### Items that could change (as a probability)

#### High

* API SMN
* API Android

#### Medium

* Language
* Data provider

#### Low
* Underlying OS
* UI app
* UI notification
* Processing the probabilities