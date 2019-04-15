-----------------------------------------------------------------------------------
-- Addon Name: Dolgubon's Lazy Writ Crafter
-- Creator: Dolgubon (Joseph Heinzle)
-- Addon Ideal: Simplifies Crafting Writs as much as possible
-- Addon Creation Date: March 14, 2016
--
-- File Name: Languages/tr.lua
-- File Description: Korean Localization
-- Load Order Requirements: None
-- 
-----------------------------------------------------------------------------------

function WritCreater.langParser(str)
	local seperater = "[ ]+"
	str = string.gsub(str,"襄","") --을
	str = string.gsub(str,"筼","") --를
	str = string.gsub(str,"_"," ") --를

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
	params[i] = str
	return params
end

WritCreater = WritCreater or {}

function WritCreater.langWritNames() --Exacts!!!  I know for german alchemy writ is Alchemistenschrieb - so ["G"] = schrieb, and ["A"]=Alchemisten
	local names = {
	["G"] = "襘窰茜筼 顕襸靜瓤.", --의뢰서를 확인한다.
	[CRAFTING_TYPE_ENCHANTING] = "篈纕羀蟬渀", --마법부여가
	[CRAFTING_TYPE_BLACKSMITHING] = "甀覥覥襴", --대장장이_6485
	[CRAFTING_TYPE_CLOTHIER] = "覬缉芬", --재봉사_6475
	[CRAFTING_TYPE_PROVISIONING] = "袔箬芬", --요리사
	[CRAFTING_TYPE_WOODWORKING] = "粩茸滵芬", --목세공사_qsub1_0_6489
	[CRAFTING_TYPE_ALCHEMY] = "蟰瀈萠芬", --연금술사
	[CRAFTING_TYPE_JEWELRYCRAFTING] = "SchmuckHandwerk", --서머셋 없음 ㅁㄹ
	}
	return names
end

function WritCreater.writCompleteStrings() -- Vital for translation
	local strings = {
	["place"] = "<紼湴襄 苁覐蟐 猣璔瓤.>", --<물건을 상자에 넣는다.>, greeting14_0_47291
	["sign"] = "Sign the Manifest",
	["masterPlace"] = "I've finished the ",
	["masterSign"] = "<Finish the job.>",
	["masterStart"] = "<Accept the contract.>",
	["Rolis Hlaalu"] = "Rolis Hlaalu", -- This is the same in most languages but ofc chinese and japanese
	["Deliver"] = "縰瓬靘瀰", --배달하기_interact_action3_0_47260
	}
	return strings
end

function WritCreater.langMasterWritNames() -- Vital
	local names = {
		["M"] 							= "masterful",
		["M1"]							= "master",
		[CRAFTING_TYPE_ALCHEMY]			= "concoction",
		[CRAFTING_TYPE_ENCHANTING]		= "glyph",
		[CRAFTING_TYPE_PROVISIONING]	= "feast",
		["plate"]						= "plate",
		["tailoring"]					= "tailoring",
		["leatherwear"]					= "leatherwear",
		["weapon"]						= "weapon",
		["shield"]						= "shield",
	}
	return names
end

function WritCreater.languageInfo() --exacts!!!

local craftInfo = 
	{
		[CRAFTING_TYPE_CLOTHIER] =
		{
			["pieces"] = --exact!!
			{
				[1] = "穜而", --로브
				[2] = "荔造", --셔츠
				[3] = "蓠縜", --신발
				[4] = "覥渑", --장갑
				[5] = "粨覐", --모자
				[6] = "縔诀", --바지
				[7] = "螴灨覥蓝", --어깨장식
				[8] = "韈箬祠", --허리띠
				[9] = "溽渑", --경갑
				[10]= "羀造", --부츠
				[11]= "锔粩维頸甀", --팔목보호대
				[12]= "鐬潬", --투구
				[13]= "瓤箬维頸甀", --다리보호대
				[14]= "螴灨维頸甀", --어깨보호대
				[15]= "纨钸", --벨트
			},
			["match"] = --exact!!! This is not the material, but rather the prefix the material gives to equipment. e.g. Homespun Robe, Linen Robe
			{
				[1] = "萘訜", --수제
				[2] = "蝄篈闬",	--아마포
				[3] = "籴闬", --면포, 목화?
				[4] = "湰緸 聄瓨", --거미 비단
				[5] = "蟐绸蓤", --에본실
				[6] = "鉬稈蓜", --크레시
				[7] = "躠蓤", --철실
				[8] = "襀茬褠", --은섬유
				[9] = "濸箼覐 躜", --그림자 천, 그림자직물
				[10]= "茠詰", --선조
				[11]= "Rawhide", --생가죽
				[12]= "Hide", --가죽
				[13]= "Leather", --가죽
				[14]= "Full-Leather", --풀 가죽
				[15]= "Fell",
				[16]= "Brigandine",
				[17]= "躠 渀諽", --철 가죽
				[18]= "轜苁瀉", --최상급
				[19]= "濸箼覐渀諽", --그림자가죽
				[20]= "竨纠痄", --루베도
			},
		},
		[CRAFTING_TYPE_BLACKSMITHING] = 
		{
			["pieces"] = --exact!!
			{
				[1] = "痄牼", --도끼
				[2] = "癔瀰", --둔기
				[3] = "満", --검
				[4] = "訄鐬痄牼", --배틀앣, 전투도끼
				[5] ="訄鐬篝遘", --마울, 전투망치
				[6] ="甀満", --대검
				[7] = "瓨満", --단검
				[8] = "饉渑", --흉갑
				[9] = "潬癐", --구두
				[10] = "湴铀稛", --건틀렛
				[11] = "韬箄", --헬름
				[12] = "渁縘", --각반
				[13] = "溬渑", --견갑
				[14] = "湰盤", --거들
			},
			["match"] = --exact!!! This is not the material, but rather the prefix the material gives to equipment. e.g. Iron Axe, Steel Axe
			{
				[1] = "躠", --철
				[2] = "Steel", --강철
				[3] = "蠤箬靠酘", --오리할콘
				[4] = "盜裌耐", --드워븐
				[5] = "蟐维瓈", --에보니
				[6] = "遼蓜瓈裀", --칼시니움
				[7] = "済祼鋀襴钸", --갈라타이트
				[8] = "萘襀", --수은
				[9] = "滵韈渕", --공허강
				[10]= "竨聄瓤襴钸", --루비다이트_INTERACT-ACT3_0_47263
			},
		},
		[CRAFTING_TYPE_WOODWORKING] = 
		{
			["pieces"] = --Exact!!!
			{
				[1] = "顜", --활
				[2] = "顔蟼", --화염
				[3] = "狉瀰", --냉기
				[4] = "訄溩", --전격
				[5] = "遘褠", --치유
				[6] = "縩锨", --방패

			},
			["match"] = --exact!!! This is not the material, but rather the prefix the material gives to equipment. e.g. Maple Bow. Oak Bow.
			{
				[1] = "瓨隍犘紴", --단풍나무
				[2] =  "踸犘紴", --참나무
				[3] =  "猈痄縤犘紴", --너도밤나무
				[4] = "馈酔箬", --히코리
				[5] = "諼粩", --주목
				[6] =  "覐覑犘紴", --자작나무
				[7] = "紼陸稈犘紴", --물푸레나무
				[8] = "篈頸渀瓈", --마호가니
				[9] = "犘襴钸袰盜", --나이트우드
				[10] = "竨聄", --루비
			},
		},
		[CRAFTING_TYPE_JEWELRYCRAFTING] = 
		{
			["pieces"] = --Exact!!!
			{
				[1] = "ring",
				[2] = "kette",

			},
			["match"] = --exact!!! This is not the material, but rather the prefix the material gives to equipment. e.g. Maple Bow. Oak Bow.
			{
				[1] = "Zinn", -- 1
				[2] = "Kupfer", -- 26
				[3] = "Silber", -- CP10
				[4] = "Elektrum", --CP80
				[5] = "Platin", -- CP150
			},

		},
		[CRAFTING_TYPE_ENCHANTING] =
		{
			["pieces"] = --exact!!
			{ --{String Identifier, ItemId, positive or negative}
				--item-type.po 124119973-0-X
				{"disease", 45841,2},
				{"蟭绑", 45841,1}, --역병-5
				{"蒤鋜緸犘 饡萘", 45833,2}, --스태미나 흡수-77
				{"篤诀遴 饡萘", 45832,2}, --매지카 흡수-78
				{"躴稥 饡萘", 45831,2}, --체력 흡수-24
				{"frost resist",45839,2},
				{"(狉瀰)",45839,1}, --(냉기)-2
				{"(鞉痙 莌聄 渐莌)", 45836,2}, --(행동 소비 감소)-81
				{"stamina recovery", 45836,1},
				{"(溽顔)", 45842,1}, --(경화)-9
				{"羄菄", 45842,2}, --분쇄
				{"霄箬讘 篹蒵", 68342,2}, --프리즘 맹습
				{"霄箬讘 縩螴", 68342,1}, --프리즘 방어
				{"(縩螴)",45849,2}, --(방어)-83
				{"(渕鋀)",45849,1}, --(강타)-82
				{"病 言靭",45837,2}, --독 저항
				{"(病)",45837,1}, --(독)-4
				{"spell harm",45848,2},
				{"(篈纕 霼靴 讝渀)",45848,1}, --(마법 피해 증가)-87
				{"magicka recovery", 45835,1},
				{"(諼紸 莌聄 渐莌)", 45835,2}, --(주문 소비 감소)-80
				{"shock resist",45840,2},
				{"(訄溩)",45840,1}, --(전격)-3
				{"health recovery",45834,1},
				{"躴稥 渐莌",45834,2}, --체력 감소-79
				{"weakening",45843,2},
				{"(紴瀰 霼靴秉)",45843,1}, --(무기 피해량)-7
				{"(闬荘 渕顔)",45846,1}, --(포션 강화)-84
				{"speed",45846,2},
				{"flame resist",45838,2},
				{"(顔蟼)",45838,1}, --(화염)-1
				{"decrease physical", 45847,2},
				{"(紼箬 霼靴 讝渀)", 45847,1}, --(물리 피해 증가)-86
				{"蒤鋜緸犘",45833,1}, --스태미나
				{"躴稥",45831,1}, --체력
				{"篤诀遴",45832,1} --매지카
			},
			["match"] = --exact!!! The names of glyphs. The prefix (in English) So trifling glyph of magicka, for example
			{
				[1] = {"铰牌篌靜", 45855}, --티끌만한
				[2] = {"蟴盱靜",45856}, --열등한
				[3] = {"靘踮襀",45857}, --하찮은
				[4] = {"緸蝽靜",45806}, --미약한
				[5] = {"蝄瓴靜",45807}, --아담한
				[6] = {"靘盱靜",45808}, --하등한
				[7] = {"襼縘訁襸",45809}, --일반적인
				[8] = {"闉濠訁襸",45810}, --평균적인
				[9] = {"渕稥靜",45811}, --강력한
				[10]= {"袰萘靜",45812}, --우수한
				[11]= {"甀瓨靜",45813}, --대단한
				[12]= {"渕甀靜",45814}, --강대한
				[13]= {"鋁裔靜",45815}, --탁월한
				[14]= {"瀰獐訁襸",45816}, --기념적인
				[15]= {"truly",{68341,68340,},}, --초월
				[16]= {"袰裔靜",{64509,64508,},}, --최상급? 우월한?

			},
			["quality"] =
			{
				{"normal",45850},
				{"誋襀",45851}, --좋은
				{"袰萘靜",45852}, --우수한
				{"epic",45853},
				{"訄茤訁襸",45854}, --전설적인
				{"", 45850} -- default, if nothing is mentioned. Default should be Ta.
			}
		},
	}

	return craftInfo

end

function WritCreater.masterWritQuality()
	return {{"episch",4},{"legendär",5}}
end

function WritCreater.langEssenceNames() --exact!

local essenceNames =  
	{
		[1] = "蠤酔", --health, 오코
		[2] = "異瓈", --stamina, 데니
		[3] = "篈酔", --magicka, 마코
	}
	return essenceNames
end

function WritCreater.langPotencyNames() --exact!! Also, these are all the positive runestones - no negatives needed.
	local potencyNames = 
	{
		[1] = "詰祼", --Lowest potency stone lvl 조라?
		[2] = "闬祼異", --포라데
		[3] = "訜祼", --제라
		[4] = "訜詰祼", --제조라
		[5] = "蠤盜祼", --오드라
		[6] = "闬詰祼", --포조라
		[7] = "蟐痄祼", --에도라
		[8] = "訜蟐祼", --제에라
		[9] = "闬祼", --포라
		[10]= "異犘祼", --데나라
		[11]= "稈祼", --레라
		[12]= "異祼痄", --데라도
		[13]= "稈釠祼", --레쿠라
		[14]= "釠祼", --쿠라
		[15]= "稈訜祼", --레제라
		[16]= "稈闬祼", --v16 potency stone, 레포라
		
	}
	return potencyNames
end

local exceptions =
{
	[1] =
	{
		["original"] = "渕躠", --강철
		["corrected"] = "Steel", --Steel
	},
	[2] =
	{
		["original"] = "竨聄 紼陸稈犘紴", --루비 물푸레나무
		["corrected"] = "竨聄", --루비
	},
}

function WritCreater.exceptions(condition)
	condition = string.gsub(condition, " "," ")
	condition = string.lower(condition)

	for i = 1, #exceptions do

		if string.find(condition, exceptions[i]["original"]) then
			condition = string.gsub(condition, exceptions[i]["original"],exceptions[i]["corrected"])
			d("exceptions.. "..condition..", "..exceptions[i]["original"])
		end
	end
	return condition
end

function WritCreater.questExceptions(condition)
	condition = string.gsub(condition, " "," ")
	return condition
end

function WritCreater.enchantExceptions(condition)

	condition = string.gsub(condition, " "," ")
	return condition
end


function WritCreater.langTutorial(i) --sentimental
	local t = {
		[5]="Hier noch ein paar Dinge die du wissen solltest.\nDer Chat-Befehl \'/dailyreset\' zeigt dir die Wartezeit an,\nbis du die nächsten Handwerksdailies machen kannst.",
		[4]="Als letzte Information: Im Standard ist das AddOn für alle Berufe aktiviert.\nDu kannst aber in den AddOn Einstellungen die gewünschten Berufe ein-/ausschalten.",
		[3]="Als Nächstes kannst du dich entscheiden, ob dieses Fenster angezeigt werden soll, solange du dich an einer Handwerksstation befindest.\nDieses Fenster zeigt dir wieviele Materialien für das Herstellen benötigt werden und wieviele du aktuell besitzt.",
		[2]="Wenn aktiv werden deine Sachen automatisch beim Betreten einer Handwerksstation hergestellt.",
		[1]="Willkommen zu Dolgubon's Lazy Writ Crafter!\nEs gibt ein paar Einstellungen die du zunächst festlegen\n solltest. Du kannst die Einstellungen jederzeit bei\nAddOn in Einstellungen >> Erweiterungen Menü ändern.",
	}
	return t[i]
end

function WritCreater.langTutorialButton(i,onOrOff) --sentimental and short pls
	local tOn = 
	{
		[1]="Standardoptionen",
		[2]="An",
		[3]="Zeigen",
		[4]="Weiter",
		[5]="Fertig",
	}
	local tOff=
	{
		[1]="Weiter",
		[2]="Aus",
		[3]="Verbergen",
	}
	if onOrOff then
		return tOn[i]
	else
		return tOff[i]
	end
end

local function proper(str)
	if type(str)== "string" then
		return zo_strformat("<<C:1>>",str)
	else
		return str
	end
end

local function runeMissingFunction (ta,essence,potency)
	local missing = {}
	if not ta["bag"] then
		missing[#missing + 1] = "|rTa|cf60000"
	end
	if not essence["bag"] then
		missing[#missing + 1] =  "|cffcc66"..essence["slot"].."|cf60000"
	end
	if not potency["bag"] then
		missing[#missing + 1] = "|c0066ff"..potency["slot"].."|r"
	end
	local text = ""
	for i = 1, #missing do
		if i ==1 then
			text = "|cf60000Glyph could not be crafted. You do not have any "..proper(missing[i])
		else
			text = text.." or "..proper(missing[i])
		end
	end
	return text
end

WritCreater.strings = {
	["runeReq"] 					= function (essence, potency) return zo_strformat("|c2dff00Crafting will require 1 |rTa|c2dff00, 1 |cffcc66<<1>>|c2dff00 and 1 |c0066ff<<2>>|r", essence, potency) end,
	["runeMissing"] 				= runeMissingFunction ,
	["notEnoughSkill"]				= "You do not have a high enough crafting skill to make the required equipment",
	["smithingMissing"] 			= "\n|cf60000You do not have enough mats|r",
	["craftAnyway"] 				= "Craft anyway",
	["smithingEnough"] 				= "\n|c2dff00You have enough mats|r",
	["craft"] 						= "|c00ff00Craft|r",
	["crafting"] 					= "|c00ff00Crafting...|r",
	["craftIncomplete"] 			= "|cf60000Crafting could not be completed.\nYou need more mats.|r",
	["moreStyle"] 					= "|cf60000You do not have any usable style stones.\nCheck your inventory, achievements, and settings|r",
	["moreStyleSettings"]			= "|cf60000You do not have any usable style stones.\nYou likely need to allow more in the Settings Menu|r",
	["moreStyleKnowledge"]			= "|cf60000You do not have any usable style stones.\nYou might need to learn to craft more styles|r",
	["dailyreset"] 					= dailyResetFunction,
	["complete"] 					= "|c00FF00Writ complete.|r",
	["craftingstopped"]				= "Crafting stopped. Please check to make sure the addon is crafting the correct item.",
	["smithingReqM"] 				= function (amount, type, more) return zo_strformat( "Crafting will use <<1>> <<2>> (|cf60000You need <<3>>|r)" ,amount, type, more) end,
	["smithingReqM2"] 				= function (amount,type,more)     return zo_strformat( "\nAs well as <<1>> <<2>> (|cf60000You need <<3>>|r)"          ,amount, type, more) end,
	["smithingReq"] 				= function (amount,type, current) return zo_strformat( "Crafting will use <<1>> <<2>> (|c2dff00<<3>> available|r)"  ,amount, type, current) end,
	["smithingReq2"] 				= function (amount,type, current) return zo_strformat( "\nAs well as <<1>> <<2>> (|c2dff00<<3>> available|r)"         ,amount, type, current) end,
	["lootReceived"]				= "<<1>> was received (You have <<2>>)",
	["lootReceivedM"]				= "<<1>> was received ",
	["countSurveys"]				= "You have <<1>> surveys",
	["countVouchers"]				= "You have <<1>> unearned Writ Vouchers",
	["includesStorage"]				= function(type) local a= {"Surveys", "Master Writs"} a = a[type] return zo_strformat("Count includes <<1>> in house storage", a) end,
	["surveys"]						= "Crafting Surveys",
	["sealedWrits"]					= "Sealed Writs",
	["masterWritEnchantToCraft"]	= function(lvl, type, quality, writCraft, writName, generalName) 
											return zo_strformat("<<t:4>> <<t:5>> <<t:6>>: Crafting a <<t:1>> Glyph of <<t:2>> at <<t:3>> quality",lvl, type, quality,
												writCraft,writName, generalName) end,
	["masterWritSmithToCraft"]		= masterWritEnchantToCraft,
	["withdrawItem"]				= function(amount, link) return "Dolgubon's Lazy Writ Crafter retrieved "..amount.." "..link end,
	['fullBag']						= "You have no open bag spaces. Please empty your bag.",
	['masterWritSave']				= "Dolgubon's Lazy Writ Crafter has saved you from accidentally accepting a master writ! Go to the settings menu to disable this option.",
}

local DivineMats =
{
	{"Geisteraugen", "Vampirherzen", "Werwolfklauen", "'Spezielle' Süßigkeiten", "Abgetrennte Hände", "Zombieeingeweide", "Fledermauslebern", "Echsenhirne", "Hexenhüte", "Destillierte Buhs", "Singende Kröten"},
	{"Sockenpuppen", "Narrenhüte", "Lachanfälle", "Rote Heringe", "Faile Eier", "Gekrönte Hochstapler", "Schlammpasteten", "Otternasen"},
	{"Feuerwerk", "Geschenke", "Ewige Lichter", "Fichtennadeln", "Wichtelhüte", "Rentierklöten"},

}

local function shouldDivinityprotocolbeactivatednowornotitshouldbeallthetimebutwhateveritlljustbeforabit()
	
	if GetDate()%10000 == 1031 then return 1 end
	if GetDate()%10000 == 401 then return 2 end
	if GetDate()%10000 == 1231 then return 3 end
	return false
end
local function wellWeShouldUseADivineMatButWeHaveNoClueWhichOneItIsSoWeNeedToAskTheGodsWhichDivineMatShouldBeUsed() local a= math.random(1, #DivineMats ) return DivineMats[a] end
local l = shouldDivinityprotocolbeactivatednowornotitshouldbeallthetimebutwhateveritlljustbeforabit()

if l then
	DivineMats = DivineMats[l]
	local DivineMat = wellWeShouldUseADivineMatButWeHaveNoClueWhichOneItIsSoWeNeedToAskTheGodsWhichDivineMatShouldBeUsed()
	WritCreater.strings.smithingReqM = function (amount, _,more) return zo_strformat( "Crafting will use <<1>> <<4>> (|cf60000You need <<3>>|r)" ,amount, type, more, DivineMat) end
	WritCreater.strings.smithingReqM2 = function (amount, _,more) return zo_strformat( "As well as <<1>> <<4>> (|cf60000You need <<3>>|r)" ,amount, type, more, DivineMat) end
	WritCreater.strings.smithingReq = function (amount, _,more) return zo_strformat( "Crafting will use <<1>> <<4>> (|c2dff00<<3>> available|r)" ,amount, type, more, DivineMat) end
	WritCreater.strings.smithingReq2 = function (amount, _,more) return zo_strformat( "As well as <<1>> <<4>> (|c2dff00<<3>> available|r)" ,amount, type, more, DivineMat) end
end


WritCreater.optionStrings = WritCreater.optionStrings or {}

WritCreater.optionStrings.nowEditing                   = "You are changing %s settings"
WritCreater.optionStrings.accountWide                  = "Account Wide"
WritCreater.optionStrings.characterSpecific            = "Character Specific"
WritCreater.optionStrings.useCharacterSettings         = "Use character settings" -- de
WritCreater.optionStrings.useCharacterSettingsTooltip  = "Use character specific settings on this character only" --de
WritCreater.optionStrings["style tooltip"]								= function (styleName, styleStone) return zo_strformat("Allow the <<1>> style, which uses the <<2>> style stone, to be used for crafting",styleName, styleStone) end 
WritCreater.optionStrings["show craft window"]							= "Show Craft Window"
WritCreater.optionStrings["show craft window tooltip"]					= "Shows the crafting window when a crafting station is open"
WritCreater.optionStrings["autocraft"]									= "AutoCraft"
WritCreater.optionStrings["autocraft tooltip"]							= "Selecting this will cause the addon to begin crafting immediately upon entering a crafting station. If the window is not shown, this will be on."
WritCreater.optionStrings["blackmithing"]								= "Blacksmithing"
WritCreater.optionStrings["blacksmithing tooltip"]						= "Turn the addon on for Blacksmithing"
WritCreater.optionStrings["clothing"]									= "Clothing"
WritCreater.optionStrings["clothing tooltip"]							= "Turn the addon on for Clothing"
WritCreater.optionStrings["enchanting"]									= "Enchanting"
WritCreater.optionStrings["enchanting tooltip"]							= "Turn the addon on for Enchanting"
WritCreater.optionStrings["alchemy"]									= "Alchemy"
WritCreater.optionStrings["alchemy tooltip"]							= "Turn the addon on for Alchemy (Bank Withdrawal only)"
WritCreater.optionStrings["provisioning"]								= "Provisioning"
WritCreater.optionStrings["provisioning tooltip"]						= "Turn the addon on for Provisioning (Bank Withdrawal only)"
WritCreater.optionStrings["woodworking"]								= "Woodworking"
WritCreater.optionStrings["woodworking tooltip"]						= "Turn the addon on for Woodworking"
WritCreater.optionStrings["jewelry crafting"]							= "Jewelry Crafting"
WritCreater.optionStrings["jewelry crafting tooltip"]					= "Turn the addon on for Jewelry Crafting"
WritCreater.optionStrings["writ grabbing"]								= "Grab writ items"
WritCreater.optionStrings["writ grabbing tooltip"]						= "Grab items required for writs (e.g. nirnroot, Ta, etc.) from the bank"
WritCreater.optionStrings["delay"]										= "Item Grab Delay"
WritCreater.optionStrings["delay tooltip"]								= "How long to wait before grabbing items from the bank (milliseconds)"
WritCreater.optionStrings["style stone menu"]							= "Style Stones Used"
WritCreater.optionStrings["style stone menu tooltip"]					= "Choose which style stones the addon will use"
WritCreater.optionStrings["send data"]									= "Send Writ Data"
WritCreater.optionStrings["send data tooltip"]							= "Send information on the rewards received from your writ boxes. No other information is sent."
WritCreater.optionStrings["exit when done"]								= "Exit crafting window"
WritCreater.optionStrings["exit when done tooltip"]						= "Exit crafting window when all crafting is completed"
WritCreater.optionStrings["automatic complete"]							= "Automatic quest dialog"
WritCreater.optionStrings["automatic complete tooltip"]					= "Automatically accepts and completes quests when at the required place"
WritCreater.optionStrings["new container"]								= "Keep new status"
WritCreater.optionStrings["new container tooltip"]						= "Keep the new status for writ reward containers"
WritCreater.optionStrings["master"]										= "Master Writs"
WritCreater.optionStrings["master tooltip"]								= "If this is ON the addon will craft Master Writs you have active"
WritCreater.optionStrings["right click to craft"]						= "Right Click to Craft"
WritCreater.optionStrings["right click to craft tooltip"]				= "If this is ON the addon will craft Master Writs you tell it to craft after right clicking a sealed writ"
WritCreater.optionStrings["crafting submenu"]							= "Trades to Craft"
WritCreater.optionStrings["crafting submenu tooltip"]					= "Turn the addon off for specific crafts"
WritCreater.optionStrings["timesavers submenu"]							= "Timesavers"
WritCreater.optionStrings["timesavers submenu tooltip"]					= "Various small timesavers"
WritCreater.optionStrings["loot container"]								= "Loot container when received"
WritCreater.optionStrings["loot container tooltip"]						= "Loot writ reward containers when you receive them"
WritCreater.optionStrings["master writ saver"]							= "Save Master Writs"
WritCreater.optionStrings["master writ saver tooltip"]					= "Prevents Master Writs from being accepted"
WritCreater.optionStrings["loot output"]								= "Valuable Reward Alert"
WritCreater.optionStrings["loot output tooltip"]						= "Output a message when valuable items are received from a writ"
WritCreater.optionStrings["autoloot behaviour"]							= "Autoloot Behaviour" -- Note that the following three come early in the settings menu, but becuse they were changed
WritCreater.optionStrings["autoloot behaviour tooltip"]					= "Choose when the addon will autoloot writ reward containers" -- they are now down below (with untranslated stuff)
WritCreater.optionStrings["autoloot behaviour choices"]					= {"Copy the setting under the Gameplay settings", "Autoloot", "Never Autoloot"}
WritCreater.optionStrings["container delay"]							= "Delay Container Looting"
WritCreater.optionStrings["container delay tooltip"]					= "Delay the autolooting of writ reward containers when you receive them"
WritCreater.optionStrings["hide when done"]								= "Hide when done"
WritCreater.optionStrings["hide when done tooltip"]						= "Hide the addon window when all items have been crafted"
WritCreater.optionStrings['reticleColour']								= "Change Reticle Colour"
WritCreater.optionStrings['reticleColourTooltip']						= "Changes the Reticle colour if you have an uncompleted or completed writ at the station"
WritCreater.optionStrings['humorlessHuskProtection']					= "Toggle house hauntings"
WritCreater.optionStrings['humorlessHuskProtectionTooltip']				= "Toggle the special Halloween house haunting on or off"
WritCreater.optionStrings['spookyScarySkeletonWarning']					= "You feel a cold chill run down your back, as if something is following you..."
WritCreater.optionStrings['humorlessHuskProtectionRitual']				= "Click here to banish the ghost"

function WritCreater.langStationNames()
	return
	{["甀覥瀰萠 訜覑甀"] = 1, ["覬缉 訜覑甀"] = 2, -- 87370069-0-1009, 10327
	 ["篈纕羀蟬 訜覑甀"] = 3,["蟰瀈萠 訜覑甀"] = 4, ["袔箬袩 顔穜"] = 5, ["粩滵 訜覑甀"] = 6, ["Jewelry Crafting Station"] = 7, } -- 10538, 891, 16387, 15982, (23935, 24043)
	 -- 대장기술 제작대, 재봉 제작대
	 -- 마법부여 제작대, 연금술 제작대, 요리용 화로, 목공 제작대, 어딨는지 모름
end

--"<<1>> erhalten"
function WritCreater.langWritRewardBoxes () return {
	[CRAFTING_TYPE_ALCHEMY] = "蟰瀈萠芬襘 袩瀰", --연금술사의 용기
	[CRAFTING_TYPE_ENCHANTING] = "篈纕羀蟬渀襘 澤话", --마법부여가의 궤짝
	[CRAFTING_TYPE_PROVISIONING] = "袔箬芬襘 熸秬緸", --요리사의 꾸러미
	[CRAFTING_TYPE_BLACKSMITHING] = "甀覥覥襴襘 苁覐", --대장장이의 상자
	[CRAFTING_TYPE_CLOTHIER] = "覬缉芬襘 渀縩", --재봉사의 가방
	[CRAFTING_TYPE_WOODWORKING] = "粩茸滵芬襘 苁覐", --목세공사의 상자
	[CRAFTING_TYPE_JEWELRYCRAFTING] = "Jewelry Crafter's Coffer",
	[8] = "訁靘紼", --적하물
}
end


function WritCreater.getTaString()
	return "鋀" --타
end

WritCreater.lang = "tr"

WritCreater.langIsMasterWritSupported = false