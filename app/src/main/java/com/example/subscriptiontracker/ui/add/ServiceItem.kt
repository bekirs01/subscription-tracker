package com.example.subscriptiontracker.ui.add

data class ServiceItem(
    val id: String,
    val name: String,
    val logoUrlLight: String? = null, // Light mode logo (original/dark version)
    val logoUrlDark: String? = null, // Dark mode logo (white/light version)
    val drawableResId: Int? = null
)

// Popular subscription services list (~100 services)
val services = listOf(
    // Streaming Services
    ServiceItem("netflix", "Netflix", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/netflix.svg", logoUrlDark = "https://logo.clearbit.com/netflix.com"),
    ServiceItem("spotify", "Spotify", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/spotify.svg", logoUrlDark = "https://logo.clearbit.com/spotify.com"),
    ServiceItem("youtube", "YouTube Premium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/youtube.svg", logoUrlDark = "https://logo.clearbit.com/youtube.com"),
    ServiceItem("amazonprime", "Amazon Prime Video", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/amazonprime.svg", logoUrlDark = "https://logo.clearbit.com/amazon.com"),
    ServiceItem("appletv", "Apple TV+", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/appletv.svg", logoUrlDark = "https://logo.clearbit.com/apple.com"),
    ServiceItem("hulu", "Hulu", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/hulu.svg", logoUrlDark = "https://logo.clearbit.com/hulu.com"),
    ServiceItem("crunchyroll", "Crunchyroll", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/crunchyroll.svg", logoUrlDark = "https://logo.clearbit.com/crunchyroll.com"),
    ServiceItem("twitch", "Twitch", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/twitch.svg", logoUrlDark = "https://logo.clearbit.com/twitch.tv"),
    
    // Music Services
    ServiceItem("applemusic", "Apple Music", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/applemusic.svg", logoUrlDark = "https://logo.clearbit.com/apple.com"),
    ServiceItem("tidal", "Tidal", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tidal.svg", logoUrlDark = "https://logo.clearbit.com/tidal.com"),
    ServiceItem("deezer", "Deezer", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/deezer.svg", logoUrlDark = "https://logo.clearbit.com/deezer.com"),
    ServiceItem("pandora", "Pandora", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/pandora.svg", logoUrlDark = "https://logo.clearbit.com/pandora.com"),
    ServiceItem("soundcloud", "SoundCloud", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/soundcloud.svg", logoUrlDark = "https://logo.clearbit.com/soundcloud.com"),
    
    // Cloud Storage & Productivity
    ServiceItem("icloud", "iCloud", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/icloud.svg", logoUrlDark = "https://logo.clearbit.com/icloud.com"),
    ServiceItem("dropbox", "Dropbox", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/dropbox.svg", logoUrlDark = "https://logo.clearbit.com/dropbox.com"),
    ServiceItem("onedrive", "OneDrive", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoftonedrive.svg", logoUrlDark = "https://logo.clearbit.com/onedrive.live.com"),
    ServiceItem("box", "Box", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/box.svg", logoUrlDark = "https://logo.clearbit.com/box.com"),
    ServiceItem("mega", "MEGA", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/mega.svg", logoUrlDark = "https://logo.clearbit.com/mega.nz"),
    
    // Software & Creative Tools
    ServiceItem("adobe", "Adobe Creative Cloud", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/adobe.svg", logoUrlDark = "https://logo.clearbit.com/adobe.com"),
    ServiceItem("microsoft365", "Microsoft 365", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoft.svg", logoUrlDark = "https://logo.clearbit.com/microsoft.com"),
    ServiceItem("notion", "Notion", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/notion.svg", logoUrlDark = "https://logo.clearbit.com/notion.so"),
    ServiceItem("figma", "Figma", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/figma.svg", logoUrlDark = "https://logo.clearbit.com/figma.com"),
    ServiceItem("canva", "Canva", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/canva.svg", logoUrlDark = "https://logo.clearbit.com/canva.com"),
    ServiceItem("sketch", "Sketch", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/sketch.svg", logoUrlDark = "https://logo.clearbit.com/sketch.com"),
    ServiceItem("affinity", "Affinity", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/affinity.svg", logoUrlDark = "https://logo.clearbit.com/affinity.serif.com"),
    
    // Development & Code
    ServiceItem("github", "GitHub", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/github.svg", logoUrlDark = "https://logo.clearbit.com/github.com"),
    ServiceItem("gitlab", "GitLab", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/gitlab.svg", logoUrlDark = "https://logo.clearbit.com/gitlab.com"),
    ServiceItem("bitbucket", "Bitbucket", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/bitbucket.svg", logoUrlDark = "https://logo.clearbit.com/bitbucket.org"),
    ServiceItem("jetbrains", "JetBrains", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/jetbrains.svg", logoUrlDark = "https://logo.clearbit.com/jetbrains.com"),
    ServiceItem("atlassian", "Atlassian", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/atlassian.svg", logoUrlDark = "https://logo.clearbit.com/atlassian.com"),
    ServiceItem("jira", "Jira", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/jira.svg", logoUrlDark = "https://logo.clearbit.com/atlassian.com"),
    ServiceItem("confluence", "Confluence", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/confluence.svg", logoUrlDark = "https://logo.clearbit.com/atlassian.com"),
    
    // Communication & Collaboration
    ServiceItem("slack", "Slack", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/slack.svg", logoUrlDark = "https://logo.clearbit.com/slack.com"),
    ServiceItem("zoom", "Zoom", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/zoom.svg", logoUrlDark = "https://logo.clearbit.com/zoom.us"),
    ServiceItem("teams", "Microsoft Teams", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoftteams.svg", logoUrlDark = "https://logo.clearbit.com/teams.microsoft.com"),
    ServiceItem("discord", "Discord", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/discord.svg", logoUrlDark = "https://logo.clearbit.com/discord.com"),
    ServiceItem("telegram", "Telegram Premium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/telegram.svg", logoUrlDark = "https://logo.clearbit.com/telegram.org"),
    
    // Social Media Premium
    ServiceItem("twitter", "X Premium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/x.svg", logoUrlDark = "https://logo.clearbit.com/x.com"),
    ServiceItem("linkedin", "LinkedIn Premium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/linkedin.svg", logoUrlDark = "https://logo.clearbit.com/linkedin.com"),
    ServiceItem("instagram", "Instagram", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/instagram.svg", logoUrlDark = "https://logo.clearbit.com/instagram.com"),
    ServiceItem("facebook", "Facebook", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/facebook.svg", logoUrlDark = "https://logo.clearbit.com/facebook.com"),
    ServiceItem("reddit", "Reddit Premium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/reddit.svg", logoUrlDark = "https://logo.clearbit.com/reddit.com"),
    
    // E-commerce & Marketplaces
    ServiceItem("amazon", "Amazon Prime", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/amazon.svg", logoUrlDark = "https://logo.clearbit.com/amazon.com"),
    ServiceItem("shopify", "Shopify", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/shopify.svg", logoUrlDark = "https://logo.clearbit.com/shopify.com"),
    ServiceItem("etsy", "Etsy", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/etsy.svg", logoUrlDark = "https://logo.clearbit.com/etsy.com"),
    ServiceItem("ebay", "eBay", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/ebay.svg", logoUrlDark = "https://logo.clearbit.com/ebay.com"),
    
    // Cloud & Infrastructure
    ServiceItem("aws", "Amazon AWS", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/amazonaws.svg", logoUrlDark = "https://logo.clearbit.com/aws.amazon.com"),
    ServiceItem("googlecloud", "Google Cloud", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/googlecloud.svg", logoUrlDark = "https://logo.clearbit.com/cloud.google.com"),
    ServiceItem("azure", "Microsoft Azure", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/microsoftazure.svg", logoUrlDark = "https://logo.clearbit.com/azure.microsoft.com"),
    ServiceItem("digitalocean", "DigitalOcean", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/digitalocean.svg", logoUrlDark = "https://logo.clearbit.com/digitalocean.com"),
    ServiceItem("heroku", "Heroku", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/heroku.svg", logoUrlDark = "https://logo.clearbit.com/heroku.com"),
    ServiceItem("vercel", "Vercel", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/vercel.svg", logoUrlDark = "https://logo.clearbit.com/vercel.com"),
    ServiceItem("netlify", "Netlify", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/netlify.svg", logoUrlDark = "https://logo.clearbit.com/netlify.com"),
    
    // Gaming
    ServiceItem("steam", "Steam", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/steam.svg", logoUrlDark = "https://logo.clearbit.com/steampowered.com"),
    ServiceItem("playstation", "PlayStation Plus", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/playstation.svg", logoUrlDark = "https://logo.clearbit.com/playstation.com"),
    ServiceItem("xbox", "Xbox Game Pass", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/xbox.svg", logoUrlDark = "https://logo.clearbit.com/xbox.com"),
    ServiceItem("epicgames", "Epic Games", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/epicgames.svg", logoUrlDark = "https://logo.clearbit.com/epicgames.com"),
    ServiceItem("nintendo", "Nintendo Switch Online", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/nintendo.svg", logoUrlDark = "https://logo.clearbit.com/nintendo.com"),
    ServiceItem("ubisoft", "Ubisoft+", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/ubisoft.svg", logoUrlDark = "https://logo.clearbit.com/ubisoft.com"),
    
    // Education & Learning
    ServiceItem("udemy", "Udemy", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/udemy.svg", logoUrlDark = "https://logo.clearbit.com/udemy.com"),
    ServiceItem("coursera", "Coursera", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/coursera.svg", logoUrlDark = "https://logo.clearbit.com/coursera.org"),
    ServiceItem("skillshare", "Skillshare", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/skillshare.svg", logoUrlDark = "https://logo.clearbit.com/skillshare.com"),
    ServiceItem("duolingo", "Duolingo Plus", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/duolingo.svg", logoUrlDark = "https://logo.clearbit.com/duolingo.com"),
    ServiceItem("khanacademy", "Khan Academy", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/khanacademy.svg", logoUrlDark = "https://logo.clearbit.com/khanacademy.org"),
    ServiceItem("pluralsight", "Pluralsight", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/pluralsight.svg", logoUrlDark = "https://logo.clearbit.com/pluralsight.com"),
    
    // AI & Productivity
    ServiceItem("chatgpt", "ChatGPT Plus", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/openai.svg", logoUrlDark = "https://logo.clearbit.com/openai.com"),
    ServiceItem("grammarly", "Grammarly Premium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/grammarly.svg", logoUrlDark = "https://logo.clearbit.com/grammarly.com"),
    
    // Dating & Social
    ServiceItem("tinder", "Tinder Plus", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/tinder.svg", logoUrlDark = "https://logo.clearbit.com/tinder.com"),
    
    // Finance & Payment
    ServiceItem("revolut", "Revolut", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/revolut.svg", logoUrlDark = "https://logo.clearbit.com/revolut.com"),
    ServiceItem("wise", "Wise", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/wise.svg", logoUrlDark = "https://logo.clearbit.com/wise.com"),
    ServiceItem("paypal", "PayPal", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/paypal.svg", logoUrlDark = "https://logo.clearbit.com/paypal.com"),
    ServiceItem("stripe", "Stripe", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/stripe.svg", logoUrlDark = "https://logo.clearbit.com/stripe.com"),
    
    // Content & Publishing
    ServiceItem("medium", "Medium", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/medium.svg", logoUrlDark = "https://logo.clearbit.com/medium.com"),
    ServiceItem("substack", "Substack", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/substack.svg", logoUrlDark = "https://logo.clearbit.com/substack.com"),
    ServiceItem("patreon", "Patreon", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/patreon.svg", logoUrlDark = "https://logo.clearbit.com/patreon.com"),
    ServiceItem("onlyfans", "OnlyFans", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/onlyfans.svg", logoUrlDark = "https://logo.clearbit.com/onlyfans.com"),
    
    // News & Media
    ServiceItem("washingtonpost", "The Washington Post", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/thewashingtonpost.svg", logoUrlDark = "https://logo.clearbit.com/washingtonpost.com"),
    
    // Regional Services
    ServiceItem("vk", "VK", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/vk.svg", logoUrlDark = "https://logo.clearbit.com/vk.com"),
    
    // VPN & Security
    ServiceItem("nordvpn", "NordVPN", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/nordvpn.svg", logoUrlDark = "https://logo.clearbit.com/nordvpn.com"),
    ServiceItem("expressvpn", "ExpressVPN", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/expressvpn.svg", logoUrlDark = "https://logo.clearbit.com/expressvpn.com"),
    ServiceItem("1password", "1Password", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/1password.svg", logoUrlDark = "https://logo.clearbit.com/1password.com"),
    ServiceItem("lastpass", "LastPass", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/lastpass.svg", logoUrlDark = "https://logo.clearbit.com/lastpass.com"),
    ServiceItem("dashlane", "Dashlane", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/dashlane.svg", logoUrlDark = "https://logo.clearbit.com/dashlane.com"),
    
    // Fitness & Health
    ServiceItem("strava", "Strava", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/strava.svg", logoUrlDark = "https://logo.clearbit.com/strava.com"),
    ServiceItem("headspace", "Headspace", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/headspace.svg", logoUrlDark = "https://logo.clearbit.com/headspace.com"),
    
    // Food & Delivery
    ServiceItem("ubereats", "Uber Eats", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/ubereats.svg", logoUrlDark = "https://logo.clearbit.com/ubereats.com"),
    ServiceItem("doordash", "DoorDash", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/doordash.svg", logoUrlDark = "https://logo.clearbit.com/doordash.com"),
    ServiceItem("grubhub", "Grubhub", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/grubhub.svg", logoUrlDark = "https://logo.clearbit.com/grubhub.com"),
    
    // Travel
    ServiceItem("airbnb", "Airbnb", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/airbnb.svg", logoUrlDark = "https://logo.clearbit.com/airbnb.com"),
    
    // Other Popular Services
    ServiceItem("google", "Google Workspace", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/google.svg", logoUrlDark = "https://logo.clearbit.com/workspace.google.com"),
    ServiceItem("googleads", "Google Ads", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/googleads.svg", logoUrlDark = "https://logo.clearbit.com/ads.google.com"),
    ServiceItem("trello", "Trello", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/trello.svg", logoUrlDark = "https://logo.clearbit.com/trello.com"),
    ServiceItem("asana", "Asana", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/asana.svg", logoUrlDark = "https://logo.clearbit.com/asana.com"),
    ServiceItem("todoist", "Todoist", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/todoist.svg", logoUrlDark = "https://logo.clearbit.com/todoist.com"),
    ServiceItem("evernote", "Evernote", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/evernote.svg", logoUrlDark = "https://logo.clearbit.com/evernote.com"),
    ServiceItem("obsidian", "Obsidian", logoUrlLight = "https://cdn.jsdelivr.net/npm/simple-icons@v9/icons/obsidian.svg", logoUrlDark = "https://logo.clearbit.com/obsidian.md")
)

