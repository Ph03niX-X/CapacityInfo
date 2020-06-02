<p align="center">
 <b>Download from Google Play:</b>
</p>

<a href="https://play.google.com/store/apps/details?id=com.ph03nix_x.capacityinfo">
<p align="center">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
alt="Get it on Google Play" width="323" height="125" border="10"/></a>
</p>


---
<p align="center">
 <b>Download Latest Stable Version from Google Drive:</b>
</p>

<a href="https://drive.google.com/file/d/1Sq2quYI_msHJVzYSVar52PDbe1vJYE_c">
<p align="center">
<img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/GDrive.png"
alt="Get it on Google Drive" width="300" border="10"/></a>
</p>

---
<p align="center">
 <b>Download Latest Beta Version from Google Drive:</b>
</p>

<a href="https://drive.google.com/file/d/13H4OsHBtRhzPsAi-kmVqmI3y0wCJ1fGW">
<p align="center">
<img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/GDrive.png"
alt="Get it on Google Drive" width="300" border="10"/></a>
</p>

---
<p align="center">
<b>Screenshots:</b>
</p>

<img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/01.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/02.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/03.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/04.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/05.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/06.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/07.jpg" width="270" height="540"> <img src="https://github.com/Ph03niX-X/CapacityInfo/blob/master/images/screenshots/08.jpg" width="270" height="540">

---
<p align="center">
<b>Instruction:</b>
</p>

1.In order for the application to show the remaining capacity, put the device on charge, while the service must be turned on.<br/>
2.For a more accurate display of the remaining capacity, charge the device fully until "Status: Charged" is displayed. <br/>
3.If charging occurs after reaching 100%, then this is normal. Modern devices continue to charge for some time after reaching 100%. <br/>
4.If the design capacity is incorrect, change it in the application settings.<br/>
5.If the current capacity and/or charge/discharge current and/or voltage is not displayed correctly, then change the unit in the application settings.

<b>ATTENTION!</b> Do not kill the service. If the shell of your device likes to "kill" applications, or completely "kills" applications when cleaning the most recently launched applications or when removing an application from the most recently launched applications, or you use power saving, add this application to all possible exceptions. If your firmware has an application startup setting, be sure to ensure that this application can start after loading the OS, if you want the application to start automatically after loading the OS.

<b>ATTENTION!</b> On Huawei & Honor, the current capacity may not display correctly. Unfortunately nothing can be done about it

---
<p align="center">
<b>FAQ:</b>
</p>

Question: How does the app work?
Answer: Residual capacity is formed on the basis of the current capacity upon reaching 100% and the status of "Charged". During charging, an "attempt" is made to find out the residual capacity by the following formula: current capacity  / (battery level / 100). If the charge level = 1% or 0%, or when it reaches 100%, charging continues, then instead of calculating, the current capacity is displayed in the remaining capacity. The current capacity is taken from the kernel of the OS, by the Android API itself, which allows you to get the current capacity without root rights. The kernel takes the current capacity from the battery itself and writes it to the "charge_counter" file (file path: /sys/class/power_supply/battery). On how accurate the current capacity depends on the battery and the core itself.

Question: What is considered to be "Capacity Added"?
Answer: "Capacity Added" is considered very simple: the current capacity - capacity, which was before you connected the charging.

Question: I have everything (or almost everything) in zeros. Why?
Answer: Because the manufacturer of your device has saved on various sensors.  And since the kernel cannot get the value, it returns 0.

Question: Very large or small numbers are shown in the current capacity, charge/discharge current, voltage.
Answer: Go to the settings, in the "Misc" section, expand all the settings and change the unit of measurement.

Question: The current capacity has ceased to be displayed, although it was previously displayed (or is displayed, but not always).
Answer: This means that the battery is coming or has already come to an end. If everything is in order with the battery, then the firmware kernel or the controller, which gives the current capacity, may be buggy.

Question: The device is not supported. Add device support.
Answer: Alas, this is impossible, since it depends primarily on the battery and secondarily on the OS kernel. Therefore, setting 1 and writing about what is not supported, as well as asking to add support for a device, is pointless. You can bet 1, but think about it three times: is it worth spoiling the average rating for the application due to the fact that your battery or core does not give up the current capacity?

---
<p align="center">
<b>Supported Languages:</b>
</p>

+ English
+ Română
+ Беларуская
+ Русский
+ Українська

---
All suggestions for improving the application should be written to E-Mail: Ph03niX-X@outlook.com or to Telegram: [@Ph03niX_X](https://t.me/Ph03niX_X)
