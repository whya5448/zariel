-----------------------------------------------------------------------------------
-- Addon Name: Dolgubon's Lazy Writ Crafter
-- Creator: Dolgubon (Joseph Heinzle)
-- Addon Ideal: Simplifies Crafting Writs as much as possible
-- Addon Creation Date: March 14, 2016
--
-- File Name: Languages/kr.lua
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

local function proper(str)
	if type(str)== "string" then
		return zo_strformat("<<C:1>>",str)
	else
		return str
	end
end

function WritCreater.langWritNames() -- Vital
	-- Exact!!!  I know for german alchemy writ is Alchemistenschrieb - so ["G"] = schrieb, and ["A"]=Alchemisten
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


function WritCreater.languageInfo() -- Vital

local craftInfo = 
	{
		[ CRAFTING_TYPE_CLOTHIER] = 
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
				[2] = "縩锨", --방패
				[3] = "顔蟼", --화염
				[4] = "狉瀰", --냉기
				[5] = "訄溩", --전격
				[6] = "遘褠", --치유
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
				[2] = "necklace",

			},
			["match"] = --exact!!! This is not the material, but rather the prefix the material gives to equipment. e.g. Maple Bow. Oak Bow.
			{
				[1] = "Pewter", -- 1
				[2] = "Copper", -- 26
				[3] = "Silver", -- CP10
				[4] = "Electrum", --CP80
				[5] = "Platinum", -- CP150
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
				[16]= {"superb",{64509,64508,},}, --최상급? 우월한?

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

function WritCreater.masterWritQuality() -- Vital . This is probably not necessary, but it stays for now because it works
	return {{"Epic",4},{"Legendary",5}}
end




function WritCreater.langEssenceNames() -- Vital

local essenceNames =  
	{
		[1] = "蠤酔", --health, 오코
		[2] = "異瓈", --stamina, 데니
		[3] = "篈酔", --magicka, 마코
	}
	return essenceNames
end

function WritCreater.langPotencyNames() -- Vital
	--exact!! Also, these are all the positive runestones - no negatives needed.
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


function WritCreater.langTutorial(i) 
	local t = {
		[5]="There's also a few things you should know.\nFirst, /dailyreset is a slash command that will tell you\nhow long until the next daily server reset.",
		[4]="Finally, you can also choose to deactivate or\nactivate this addon for each profession.\nBy default, all applicable crafts are on.\nIf you wish to turn some off, please check the settings.",
		[3]="Next, you need to choose if you wish to see this\nwindow when using a crafting station.\nThe window will tell you how many mats the writ will require, as well as how many you currently have.",
		[2]="The first setting to choose is if you\nwant to useAutoCraft.\nIf on, when you enter a crafting station, the addon will start crafting.",
		[1]="Welcome to Dolgubon's Lazy Writ Crafter!\nThere are a few settings you should choose first.\n You can change the settings at any\n time in the settings menu.",
	}
	return t[i]
end

function WritCreater.langTutorialButton(i,onOrOff) -- sentimental and short please. These must fit on a small button
	local tOn = 
	{
		[1]="Use Defaults",
		[2]="On",
		[3]="Show",
		[4]="Continue",
		[5]="Finish",
	}
	local tOff=
	{
		[1]="Continue",
		[2]="Off",
		[3]="Do not show",
	}
	if onOrOff then
		return tOn[i]
	else
		return tOff[i]
	end
end

function WritCreater.langStationNames()
	return
	{["甀覥瀰萠 訜覑甀"] = 1, ["覬缉 訜覑甀"] = 2, -- 87370069-0-1009, 10327
		["篈纕羀蟬 訜覑甀"] = 3,["蟰瀈萠 訜覑甀"] = 4, ["袔箬袩 顔穜"] = 5, ["粩滵 訜覑甀"] = 6, ["Jewelry Crafting Station"] = 7, } -- 10538, 891, 16387, 15982, (23935, 24043)
	-- 대장기술 제작대, 재봉 제작대
	-- 마법부여 제작대, 연금술 제작대, 요리용 화로, 목공 제작대, 어딨는지 모름
end

-- What is this??! This is just a fun 'easter egg' that is never activated on easter.
-- Replaces mat names with a random DivineMats on Halloween, New Year's, and April Fools day. You don't need this many! :D
-- Translate it or don't, completely up to you. But if you don't translate it, replace the body of 
-- shouldDivinityprotocolbeactivatednowornotitshouldbeallthetimebutwhateveritlljustbeforabit()
-- with just a return false. (This will prevent it from ever activating. Also, if you're a user and don't like this,
-- you're boring, and also that's how you can disable it. )
local DivineMats =
{
	{"Ghost Eyes", "Vampire Hearts", "Werewolf Claws", "'Special' Candy", "Chopped Hands", "Zombie Guts", "Bat Livers", "Lizard Brains", "Witches Hats", "Distilled Boos", "Singing Toads"},
	{"Sock Puppets", "Jester Hats","Otter Noses",  "|cFFC300Tempering Alloys|r", "Red Herrings", "Rotten Tomatoes","Fake Oil of Life", "Crowned Imposters", "Mudpies"},
	{"Fireworks", "Presents", "Crackers", "Reindeer Bells", "Elven Hats", "Pine Needles", "Essences of Time", "Ephemeral Lights"},
}

-- confetti?
-- random sounds?
-- 

local function shouldDivinityprotocolbeactivatednowornotitshouldbeallthetimebutwhateveritlljustbeforabit()
	if GetDate()%10000 == 1031 then return 1 end
	if GetDate()%10000 == 401 then return 2 end
	if GetDate()%10000 == 1231 then return 3 end
	return false
end


local function hasMadnessEngulfedNirn()
	local t = GetTimeString() local c = string.sub(t, 1,string.find(t, ":") - 1)
	return tonumber(c) > 11
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

local function hasMadnessEngulfedNirn()
	local t = GetTimeString() local c = string.sub(t, 1,string.find(t, ":") - 1)
	return tonumber(c) > 11
end


-- [[ /script local writcreater = {} local c = {a = 1} local g = {__index = c} setmetatable(writ, g) d(a.a) local e = {__index = {Z = 2}} setmetatable(c, e) d(a.Z)
local h = {__index = {}}
local t = {}
local g = {["__index"] = t}
setmetatable(t, h)
setmetatable(WritCreater, g) --]]

local function enableAlternateUniverse(override)

	if shouldDivinityprotocolbeactivatednowornotitshouldbeallthetimebutwhateveritlljustbeforabit() == 1 or override then
	--if true then
		local stations = 
			{"Blacksmithing Station", "Clothing Station", "Enchanting Table",
			"Alchemy Station",  "Cooking Fire", "Woodworking Station","Jewelry Crafting Station",  "Outfit Station", "Transmute Station", "Wayshrine"}
			local stationNames =  -- in the comments are other names that were also considered, though not all were considered seriously
			{"Wightsmithing Station", -- Popcorn Machine , Skyforge, Heavy Metal Station, Metal Clockwork Solid, Wightsmithing Station., Coyote Stopper
			 "Sock Puppet Theatre", -- Sock Distribution Center, Soul-Shriven Sock Station, Grandma's Sock Knitting Station, Knits and Pieces, Sock Knitting Station
			"Top Hats Inc.", -- Mahjong Station, Magic Store, Card Finder, Five Aces, Top Hat Store
			"Seedy Skooma Bar", -- Chemical Laboratory , Drugstore, White's Garage, Cocktail Bar, Med-Tek Pharmaceutical Company, Med-Tek Laboratories, Skooma Central, Skooma Backdoor Dealers, Sheogorath's Pharmacy
			 "Khajit Fried Chicken", -- Khajit Fried Chicken, soup Kitchen, Some kind of bar, misspelling?, Roast Bosmer
			 "IKEA Assembly Station", -- Chainsaw Massace, Saw Station, Shield Corp, IKEA Assembly Station, Wood Splinter Removal Station
			 "April Fool's Gold",--"Diamond Scam Store", -- Lucy in the Sky, Wedding Planning Hub, Shiny Maker, Oooh Shiny, Shiny Bling Maker, Cubit Zirconia, Rhinestone Palace
			 -- April Fool's Gold
			 "Khajit Fur Trade Outpost", -- Jester Dressing Room Loincloth Shop, Khajit Walk, Khajit Fashion Show, Mummy Maker, Thalmor Spy Agency, Tamriel Catwalk, 
			 --	Tamriel Khajitwalk, second hand warehouse,. Dye for Me, Catfur Jackets, Outfit station "Khajiit Furriers", Khajit Fur Trading Outpost
			 "Sacrificial Goat Altar",-- Heisenberg's Station Correction Facility, Time Machine, Probability Redistributor, Slot Machine Rigger, RNG Countermeasure, Lootcifer Shrine, Whack-a-mole
			 -- Anti Salt Machine, Department of Corrections, Quantum State Rigger , Unnerf Station
			 "TARDIS" } -- Transporter, Molecular Discombobulator, Beamer, Warp Tunnel, Portal, Stargate, Cannon!, Warp Gate
			
			local crafts = {"Blacksmithing", "Clothing", "Enchanting","Alchemy","Provisioning","Woodworking","Jewelry Crafting" }
			local craftNames = {
				"Wightsmithing",
				"Sock Knitting",
				"Top Hat Tricks",
				"Skooma Brewing",
				"Chicken Frying",
				"IKEA Assembly",
				"Fool's Gold Creation",
			}
			local quest = {"Blacksmith", "Clothier", "Enchanter" ,"Alchemist", "Provisioner", "Woodworker", "Jewelry Crafting"}
			local questNames = 	
			{
				"Wightsmith",
				"Sock Knitter",
				"Top Hat Trickster",
				"Skooma Brewer",
				"Chicken Fryer",
				"IKEA Assembly",
				"Fool's Gold",
			}
			local items = {"Blacksmith", "Clothier", "Enchanter", "alchemical", "food and drink",  "Woodworker", "Jewelry"}
			local itemNames = {
				"Wight",
				"Sock Puppet",
				"Top Hat",
				"Skooma",
				"Fried Chicken",
				"IKEA",
				"Fool's Gold",
			}
			local coffers = {"Blacksmith", "Clothier", "Enchanter" ,"Alchemist", "Provisioner", "Woodworker", "Jewelry Crafter's",}
			local cofferNames = {
				"Wightsmith",
				"Sock Knitter",
				"Top Hat Trickster",
				"Skooma Brewer",
				"Chicken Fryer",
				"IKEA Assembly",
				"Fool's Gold",
			}
		

		local t = {["__index"] = {}}
		function h.__index.alternateUniverse()
			return stations, stationNames
		end
		function h.__index.alternateUniverseCrafts()
			return crafts, craftNames
		end
		function h.__index.alternateUniverseQuests()
			return quest, questNames
		end
		function h.__index.alternateUniverseItems()
			return items, itemNames
		end
		function h.__index.alternateUniverseCoffers()
			return coffers, cofferNames
		end

		h.__metatable = "No looky!"
		local a = WritCreater.langStationNames()
		a[1] = 1
		for i = 1, 7 do
			a[stationNames[i]] = i
		end
		WritCreater.langStationNames = function() 
			return a
		end
		local b =WritCreater.langWritNames()
		for i = 1, 7 do
			b[i] = questNames[i]
		end
		-- WritCreater.langWritNames = function() return b end

	end
end

-- For Transmutation: "Well Fitted Forever"
-- So far, I like blacksmithing, clothing, woodworking, and wayshrine, enchanting
-- that leaves , alchemy, cooking, jewelry, outfits, and transmutation

local lastYearStations = 
{"Blacksmithing Station", "Clothing Station", "Woodworking Station", "Cooking Fire", 
"Enchanting Table", "Alchemy Station", "Outfit Station", "Transmute Station", "Wayshrine"}
local stationNames =  -- in the comments are other names that were also considered, though not all were considered seriously
{"Heavy Metal 112.3 FM", -- Popcorn Machine , Skyforge, Heavy Metal Station
 "Sock Knitting Station", -- Sock Distribution Center, Soul-Shriven Sock Station, Grandma's Sock Knitting Station, Knits and Pieces
 "Splinter Removal Station", -- Chainsaw Massace, Saw Station, Shield Corp, IKEA Assembly Station, Wood Splinter Removal Station
 "McSheo's Food Co.", 
 "Tetris Station", -- Mahjong Station
 "Poison Control Centre", -- Chemical Laboratory , Drugstore, White's Garage, Cocktail Bar, Med-Tek Pharmaceutical Company, Med-Tek Laboratories
 "Thalmor Spy Agency", -- Jester Dressing Room Loincloth Shop, Khajit Walk, Khajit Fashion Show, Mummy Maker, Thalmor Spy Agency, Morag Tong Information Hub, Tamriel Spy HQ, 
 "Department of Corrections",-- Heisenberg's Station Correction Facility, Time Machine, Probability Redistributor, Slot Machine Rigger, RNG Countermeasure, Lootcifer Shrine, Whack-a-mole
 -- Anti Salt Machine, Department of Corrections
 "Warp Gate" } -- Transporter, Molecular Discombobulator, Beamer, Warp Tunnel, Portal, Stargate, Cannon!, Warp Gate

enableAlternateUniverse(GetDisplayName()=="@Dolgubon")


--Hide craft window when done
--"Verstecke Fenster anschließend",
-- [tooltip ] = "Verstecke das Writ Crafter Fenster an der Handwerksstation automatisch, nachdem die Gegenstände hergestellt wurden"

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



WritCreater.lang = "kr"
WritCreater.langIsMasterWritSupported = false

--[[
SLASH_COMMANDS['/opencontainers'] = function()local a=WritCreater.langWritRewardBoxes() for i=1,200 do for j=1,6 do if a[j]==GetItemName(1,i) then if IsProtectedFunction("endUseItem") then
	CallSecureProtected("endUseItem",1,i)
else
	UseItem(1,i)
end end end end end]]
