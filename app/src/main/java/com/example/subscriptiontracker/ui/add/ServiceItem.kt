package com.example.subscriptiontracker.ui.add

data class ServiceItem(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val drawableResId: Int? = null
)

// Popular subscription services list (~100 services)
val services = listOf(
    // Streaming Services
    ServiceItem("netflix", "Netflix", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/netflix.svg"),
    ServiceItem("spotify", "Spotify", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/spotify.svg"),
    ServiceItem("youtube", "YouTube Premium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/youtube.svg"),
    ServiceItem("disneyplus", "Disney+", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/disneyplus.svg"),
    ServiceItem("hbo", "HBO Max", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/hbomax.svg"),
    ServiceItem("amazonprime", "Amazon Prime Video", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/amazonprime.svg"),
    ServiceItem("appletv", "Apple TV+", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/appletv.svg"),
    ServiceItem("paramount", "Paramount+", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/paramountplus.svg"),
    ServiceItem("hulu", "Hulu", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/hulu.svg"),
    ServiceItem("peacock", "Peacock", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/peacock.svg"),
    ServiceItem("crunchyroll", "Crunchyroll", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/crunchyroll.svg"),
    ServiceItem("twitch", "Twitch", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/twitch.svg"),
    
    // Music Services
    ServiceItem("applemusic", "Apple Music", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/applemusic.svg"),
    ServiceItem("tidal", "Tidal", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tidal.svg"),
    ServiceItem("deezer", "Deezer", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/deezer.svg"),
    ServiceItem("pandora", "Pandora", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/pandora.svg"),
    ServiceItem("soundcloud", "SoundCloud", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/soundcloud.svg"),
    
    // Cloud Storage & Productivity
    ServiceItem("icloud", "iCloud", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/icloud.svg"),
    ServiceItem("googleone", "Google One", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/googleone.svg"),
    ServiceItem("dropbox", "Dropbox", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/dropbox.svg"),
    ServiceItem("onedrive", "OneDrive", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoftonedrive.svg"),
    ServiceItem("box", "Box", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/box.svg"),
    ServiceItem("pcloud", "pCloud", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/pcloud.svg"),
    ServiceItem("mega", "MEGA", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/mega.svg"),
    
    // Software & Creative Tools
    ServiceItem("adobe", "Adobe Creative Cloud", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/adobe.svg"),
    ServiceItem("microsoft365", "Microsoft 365", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoft.svg"),
    ServiceItem("notion", "Notion", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/notion.svg"),
    ServiceItem("figma", "Figma", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/figma.svg"),
    ServiceItem("canva", "Canva", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/canva.svg"),
    ServiceItem("sketch", "Sketch", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/sketch.svg"),
    ServiceItem("affinity", "Affinity", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/affinity.svg"),
    
    // Development & Code
    ServiceItem("github", "GitHub", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/github.svg"),
    ServiceItem("gitlab", "GitLab", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/gitlab.svg"),
    ServiceItem("bitbucket", "Bitbucket", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/bitbucket.svg"),
    ServiceItem("jetbrains", "JetBrains", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/jetbrains.svg"),
    ServiceItem("atlassian", "Atlassian", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/atlassian.svg"),
    ServiceItem("jira", "Jira", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/jira.svg"),
    ServiceItem("confluence", "Confluence", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/confluence.svg"),
    
    // Communication & Collaboration
    ServiceItem("slack", "Slack", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/slack.svg"),
    ServiceItem("zoom", "Zoom", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/zoom.svg"),
    ServiceItem("teams", "Microsoft Teams", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoftteams.svg"),
    ServiceItem("discord", "Discord", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/discord.svg"),
    ServiceItem("telegram", "Telegram Premium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/telegram.svg"),
    
    // Social Media Premium
    ServiceItem("twitter", "X Premium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/x.svg"),
    ServiceItem("linkedin", "LinkedIn Premium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/linkedin.svg"),
    ServiceItem("instagram", "Instagram", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/instagram.svg"),
    ServiceItem("facebook", "Facebook", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/facebook.svg"),
    ServiceItem("reddit", "Reddit Premium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/reddit.svg"),
    
    // E-commerce & Marketplaces
    ServiceItem("amazon", "Amazon Prime", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/amazon.svg"),
    ServiceItem("shopify", "Shopify", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/shopify.svg"),
    ServiceItem("etsy", "Etsy", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/etsy.svg"),
    ServiceItem("ebay", "eBay", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/ebay.svg"),
    
    // Cloud & Infrastructure
    ServiceItem("aws", "Amazon AWS", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/amazonaws.svg"),
    ServiceItem("googlecloud", "Google Cloud", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/googlecloud.svg"),
    ServiceItem("azure", "Microsoft Azure", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoftazure.svg"),
    ServiceItem("digitalocean", "DigitalOcean", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/digitalocean.svg"),
    ServiceItem("heroku", "Heroku", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/heroku.svg"),
    ServiceItem("vercel", "Vercel", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/vercel.svg"),
    ServiceItem("netlify", "Netlify", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/netlify.svg"),
    
    // Gaming
    ServiceItem("steam", "Steam", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/steam.svg"),
    ServiceItem("playstation", "PlayStation Plus", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/playstation.svg"),
    ServiceItem("xbox", "Xbox Game Pass", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/xbox.svg"),
    ServiceItem("epicgames", "Epic Games", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/epicgames.svg"),
    ServiceItem("nintendo", "Nintendo Switch Online", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/nintendo.svg"),
    ServiceItem("ubisoft", "Ubisoft+", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/ubisoft.svg"),
    ServiceItem("ea", "EA Play", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/electronicarts.svg"),
    
    // Education & Learning
    ServiceItem("udemy", "Udemy", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/udemy.svg"),
    ServiceItem("coursera", "Coursera", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/coursera.svg"),
    ServiceItem("skillshare", "Skillshare", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/skillshare.svg"),
    ServiceItem("duolingo", "Duolingo Plus", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/duolingo.svg"),
    ServiceItem("masterclass", "MasterClass", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/masterclass.svg"),
    ServiceItem("khanacademy", "Khan Academy", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/khanacademy.svg"),
    ServiceItem("pluralsight", "Pluralsight", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/pluralsight.svg"),
    
    // AI & Productivity
    ServiceItem("chatgpt", "ChatGPT Plus", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/openai.svg"),
    ServiceItem("claude", "Claude Pro", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/anthropic.svg"),
    ServiceItem("midjourney", "Midjourney", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/midjourney.svg"),
    ServiceItem("grammarly", "Grammarly Premium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/grammarly.svg"),
    
    // Dating & Social
    ServiceItem("tinder", "Tinder Plus", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tinder.svg"),
    ServiceItem("bumble", "Bumble", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/bumble.svg"),
    
    // Finance & Payment
    ServiceItem("revolut", "Revolut", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/revolut.svg"),
    ServiceItem("wise", "Wise", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/wise.svg"),
    ServiceItem("paypal", "PayPal", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/paypal.svg"),
    ServiceItem("stripe", "Stripe", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/stripe.svg"),
    
    // Content & Publishing
    ServiceItem("medium", "Medium", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/medium.svg"),
    ServiceItem("substack", "Substack", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/substack.svg"),
    ServiceItem("patreon", "Patreon", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/patreon.svg"),
    ServiceItem("onlyfans", "OnlyFans", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/onlyfans.svg"),
    
    // News & Media
    ServiceItem("newyorktimes", "The New York Times", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/thenewyorktimes.svg"),
    ServiceItem("washingtonpost", "The Washington Post", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/thewashingtonpost.svg"),
    ServiceItem("wallstreetjournal", "The Wall Street Journal", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/thewallstreetjournal.svg"),
    
    // Regional Services
    ServiceItem("yandex", "Yandex Plus", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/yandex.svg"),
    ServiceItem("vk", "VK", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/vk.svg"),
    
    // VPN & Security
    ServiceItem("nordvpn", "NordVPN", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/nordvpn.svg"),
    ServiceItem("expressvpn", "ExpressVPN", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/expressvpn.svg"),
    ServiceItem("surfshark", "Surfshark", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/surfshark.svg"),
    ServiceItem("1password", "1Password", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/1password.svg"),
    ServiceItem("lastpass", "LastPass", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/lastpass.svg"),
    ServiceItem("dashlane", "Dashlane", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/dashlane.svg"),
    
    // Fitness & Health
    ServiceItem("strava", "Strava", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/strava.svg"),
    ServiceItem("myfitnesspal", "MyFitnessPal", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/myfitnesspal.svg"),
    ServiceItem("headspace", "Headspace", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/headspace.svg"),
    ServiceItem("calm", "Calm", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/calm.svg"),
    
    // Food & Delivery
    ServiceItem("ubereats", "Uber Eats", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/ubereats.svg"),
    ServiceItem("doordash", "DoorDash", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/doordash.svg"),
    ServiceItem("grubhub", "Grubhub", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/grubhub.svg"),
    
    // Travel
    ServiceItem("airbnb", "Airbnb", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/airbnb.svg"),
    ServiceItem("booking", "Booking.com", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/bookingdotcom.svg"),
    
    // Other Popular Services
    ServiceItem("google", "Google Workspace", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/google.svg"),
    ServiceItem("googleads", "Google Ads", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/googleads.svg"),
    ServiceItem("trello", "Trello", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/trello.svg"),
    ServiceItem("asana", "Asana", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/asana.svg"),
    ServiceItem("todoist", "Todoist", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/todoist.svg"),
    ServiceItem("evernote", "Evernote", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/evernote.svg"),
    ServiceItem("obsidian", "Obsidian", "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/obsidian.svg")
)

