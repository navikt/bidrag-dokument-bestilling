// @ts-nocheck
const fs = require('fs');

const axios = require('axios');
const sjablonTyperString = fs.readFileSync('./sjablontyper.json', 'utf8')
const sjablontyperJson = JSON.parse(sjablonTyperString)

const result = sjablontyperJson.map((value)=>`"${value.type}" to "${value.beskrivelse?.trim()}"`).join(",\n")
const result2 = sjablontyperJson
.filter((value)=>value.beskrivelse?.trim().length > 1)
.map((value)=>`${value.beskrivelse?.trim()?.toUpperCase()
.replace(/[%]/g, "PROSENT")
.replace(/[().]/g, "")
.replace(/[-/" "]/g, "_")}("${value.type?.trim()}")`).join(",\n")

console.log(result2)