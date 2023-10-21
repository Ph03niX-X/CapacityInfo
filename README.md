<p align="center">
 <b>Download from Google Play:</b>
</p>

<a href="https://play.google.com/store/apps/details?id=com.ph03nix_x.capacityinfo">
<p align="center">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
alt="Get it on Google Play" width="323" height="125" border="10"/></p></a>


---
<p align="center">
 <b>Download Latest Stable Version from Google Drive:</b>
</p>

<a href="https://drive.google.com/file/d/1_mqRV2xoH5cW3gJOVlLDHnNi2OOm7xjv">
<p align="center">
<img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/GDrive.png"
alt="Get it on Google Drive" width="300" border="10"/></p></a>

---
<p align="center">
 <b>Download Latest Beta Version from Google Drive:</b>
</p>

<a href="https://drive.google.com/file/d/1HJB0UWKlLXw7q-j2Ka3U8PypZbOZEChL">
<p align="center">
<img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/GDrive.png"
alt="Get it on Google Drive" width="300" border="10"/></p></a>

---
<p align="center">
<b>Screenshots:</b>
</p>

<img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/01.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/02.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/03.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/04.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/05.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/06.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/07.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/08.jpg" width="270" height="540">

---
<p align="center">
<b>About:</b>
</p>

Do you want to know the remaining battery capacity of your smartphone or tablet, or have you bought a new battery and want to check its capacity? Then this app is for you! Capacity Info will help you to know the remaining battery capacity or to know the actual capacity of a new battery. Also with this application you can find out the capacity in Wh, the number of charge cycles, the temperature and voltage of the battery, find out the charging/discharging current, receive notifications when the battery is low (the charge level is adjustable), when the battery is charged to a certain charge level, when the battery is full charged (Status "Charged"). Also with the help of this application you can receive notifications of overheating/overcooling of the battery, and also with the help of this application you can find out the limit of the charging current (not everywhere it is possible to get data on the limit of the charging current). It is also possible to display values in an overlay and much more.

P.S This application consumes <b>very little</b> background power. Therefore, using this application, you will not notice the loss of autonomy

<b>Application features:</b>
• Battery Wear;<br/>
• Residual capacity;<br/>
• Added capacity during charging;<br/>
• Current capacity;<br/>
• Charge level (%);<br/>
• Charging status;<br/>
• Charging/discharging current;<br/>
• Maximum, average and minimum charge/discharge current;<br/>
• Fast Charge: Yes (Watt)/No;<br/>
• Battery temperature;<br/>
• Maximum, average and minimum battery temperature;<br/>
• Battery voltage;<br/>
• Number of cycles;<br/>
• Number of charges;<br/>
• Battery status;<br/>
• Last charge time;<br/>
• Battery technology;<br/>
• History of full charges;<br/>
• Notification of full charge, certain level (%) of charge, certain level (%) of discharge, overheating and overcooling;<br/>
• Overlay;<br/>
• Capacity in Wh;<br/>
• Charge/Discharge current in Watt;<br/>
• And much more<br/>

<b>Attention!</b> Before leaving a review or asking a question, read the instructions and follow them, as well as read the FAQ, there are answers to many questions.

P.S If you have any suggestions for improving the application or you have found any bug or error, write to the E-Mail: Ph03niX-X@outlook.com or Telegram: @Ph03niX_X or open an Issue on GitHub

---
<p align="center">
<b>Instruction:</b>
</p>

1.In order for the application to show the residual capacity, discharge the battery to 10% or lower and put the device on charge, while the service must be turned on.<br/>
2.For a more accurate display of the remaining capacity, charge the device fully until "Status: Charged" is displayed.<br/>
3.If charging occurs after reaching 100%, then this is normal. Modern devices continue to charge for some time after reaching 100%.<br/>
4.If the design capacity is incorrect, change it in the application settings.<br/>
5.If the current capacity and/or charge/discharge current and/or voltage is not displayed correctly, then change the unit in the application settings.

<b>ATTENTION!</b> Do not kill the service. If the shell of your device likes to "kill" applications, or completely "kills" applications when cleaning the most recently launched applications or when removing an application from the most recently launched applications, or you use power saving, add this application to all possible exceptions. If your firmware has an application startup setting, be sure to ensure that this application can start after loading the OS, if you want the application to start automatically after loading the OS. If you don't know what and where to configure so that the application is not "killed" by the system, use the "Don't kill my app!" Service by choosing the manufacturer of your device

---
<p align="center">
<b>FAQ:</b>
</p>

Question: How does the app work?<br/>
Answer: Residual capacity is formed on the basis of the current capacity upon reaching 100% and the status of "Charged". The current capacity is taken from the kernel of the OS, by the Android API itself, which allows you to get the current capacity without root rights. The kernel takes the current capacity from the battery itself and writes it to the "charge_counter" file (file path: /sys/class/power_supply/battery). On how accurate the current capacity depends on the battery and the kernel itself.

Question: What is considered to be "Capacity Added"?<br/>
Answer: "Capacity Added" is considered very simple: the current capacity - capacity, which was before you connected the charging.

Question: Where does the application get the "Charging Current Limit"?<br/>
Answer: The application takes the "Charging Current Limit" from the kernel file "constant_charge_current_max" along the path /sys/class/power_supply/battery.

Question: Why is "Charging Current Limit" not displayed?<br/>
Answer: The display "Charging Current Limit" depends on whether the file "constant_charge_current_max" is readable or not. Not all kernels allow "reading" this file, and also not all kernels have this file.

Question: I have everything (or almost everything) in zeros. Why?<br/>
Answer: Because the manufacturer of your device has saved on various sensors. And since the kernel cannot get the value, it returns 0.

Question: Very large or small numbers are shown in the current capacity, charge/discharge current, voltage.<br/>
Answer: Go to the settings, in the "Misc" section, expand all the settings and change the unit of measurement.

Question: The current capacity has ceased to be displayed, although it was previously displayed (or is displayed, but not always).<br/>
Answer: This means that the battery is coming or has already come to an end. If everything is in order with the battery, then the firmware kernel or the controller, which gives the current capacity, may be buggy.

Question: Residual capacity is higher than the design capacity<br/>
Answer: If the battery is new, this is normal. The capacity of a battery, especially a new one, is never equal to the design capacity, it is either 2–3% less, or higher, but most often lower. If the capacity of the new battery is higher than the design one, then consider yourself lucky with the battery. If the battery is not new, you can try to calibrate the kernel by discharging the battery to 10% or lower and charging it to full 100%. If this does not help, then unfortunately either the kernel developers made a mistake somewhere that incorrectly reads the current capacity from the battery, or the matter is in the battery itself.

Question: Battery wear changes when the charger is disconnected<br/>
Answer: The fact is that when the charger is disconnected, sometimes the current capacity differs from what it was when fully charged, thus, when the charger is disconnected, the battery wear is more true than before the charger was disconnected.

Question: Battery wear does not change even after several months<br/>
Answer: Sometimes the current capacity may not be displayed correctly and due to this the battery wear will be incorrect. In order to avoid this, calibrate the current capacity once a month or two. In order to calibrate the current capacity, you need to discharge the battery to 10% or lower and charge to 100% (status: "Fully Charged")

Question: With each charge, the battery wear changes, then increases, then decreases<br/>
Answer: This is normal, since chemical processes occur in the battery.

Question: Where does the application get the "Number of Cycles (Android)"?<br/>
Answer: The application gets data from the "cycles_count" kernel file at the path: /sys/class/power_supply/battery.

Question: Not Displayed "Number of Cycles (Android)"<br/>
Answer: If "Number of Cycles (Android)" is not displayed, then it is not possible to read the file in which the number of charging cycles is recorded due to the fact that read access is closed

---
<p align="center">
<b>Tips:</b>
</p>

Tip #1: Try not to discharge the battery below 10%

Tip #2: Do not overcool (0°C/32°F and below) and do not overheat the battery (40+°C/104+°F). Overcooling and overheating negatively affect battery life.

Tip #3: Avoid holding a fully charged battery for a long time. The longer you keep a fully charged battery on charge, the more often the battery recharges, and each recharge reduces the number of cycles.

Tip #4: Try not to use the device when fully charged while the charger is connected. If you use the device with a fully charged battery when the charger is connected, then 2 things happen - heating, which negatively affects the battery life and frequent recharging, which reduces the number of cycles
