WritCreater = WritCreater or {}
function WritCreater.initializeReticleChanges()
	if not WritCreater.langStationNames then return end
	local stations = WritCreater.langStationNames()
	local originalReticleFunction = ZO_ReticleContainerInteractContext.SetText
	local deliverText = WritCreater.writCompleteStrings()["Deliver"]

	local function isQuestComplete(questIndex, numConditions)
		local numConditions = GetJournalQuestNumConditions(questIndex)
		for i = 1, numConditions do

			local cur, max =  GetJournalQuestConditionValues(questIndex,1,i)

			local s = GetJournalQuestConditionInfo(questIndex,1,i)

			if string.find(s, deliverText) then  return true
			end
		end
		return false
	end

	local function parser(str)
		local seperater = "[-_]+"

		str = string.gsub(str,":"," ")

		local params = {}
		local i = 1
		local searchResult1, searchResult2  = string.find(str,seperater)
		if searchResult1 == 1 then
			str = string.sub(str, searchResult2+1)
			searchResult1, searchResult2  = string.find(str,seperater)
		end

		while searchResult1 do
			params[i] = string.sub(str, 1, searchResult1-1)
			str = string.sub(str, searchResult2+1)
			searchResult1, searchResult2  = string.find(str,seperater)
			i=i+1
		end
		return str

	end

	local function setupReplacement(object, functionName)

		local original = object[functionName]
		object[functionName] = function(self, text)
		-- If the setting is off exit

		if not WritCreater:GetSettings().changeReticle then  original(self, text) return end
			local dispText = text;
			if WritCreater.lang == "tr" then
			text = parser(text)
			end

			-- if not a station exit
			local craftingType = stations[text]
			if not craftingType then  original(self, text) return end

			-- Otherwise, do we have a writ on?
			local writs = WritCreater.writSearch()
			if writs[craftingType] then

				--d(text)
				--d(dispText)
				--d(writs[craftingType])
				--d(isQuestComplete(writs[craftingType]))
				--d(GetJournalQuestIsComplete(writs[craftingType]))

				-- we have a writ. Do we need the reticle green or red
				if isQuestComplete(writs[craftingType]) or GetJournalQuestIsComplete(writs[craftingType]) then
					text = "|c66ff66"..dispText.."|r"
				else
					text = "|cff6666"..dispText.."|r"
				end

				original(self, text)
			else
				original(self, text)
			end
		end
	end

	setupReplacement(ZO_ReticleContainerInteractContext, "SetText")
end