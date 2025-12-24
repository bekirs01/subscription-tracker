package com.example.subscriptiontracker.data

import com.example.subscriptiontracker.PopularService

object PopularServices {
    val top100 = listOf(
        // Streaming Services
        PopularService("netflix", "Netflix", "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg", "Streaming"),
        PopularService("youtube", "YouTube Premium", "https://upload.wikimedia.org/wikipedia/commons/0/09/YouTube_full-color_icon_%282017%29.svg", "Streaming"),
        PopularService("spotify", "Spotify", "https://upload.wikimedia.org/wikipedia/commons/1/19/Spotify_logo_without_text.svg", "Music"),
        PopularService("disney", "Disney+", "https://upload.wikimedia.org/wikipedia/commons/3/3e/Disney%2B_logo.svg", "Streaming"),
        PopularService("amazon_prime", "Amazon Prime", "https://upload.wikimedia.org/wikipedia/commons/f/f1/Prime_Video.png", "Streaming"),
        PopularService("hbo_max", "HBO Max", "https://upload.wikimedia.org/wikipedia/commons/1/17/HBO_Max_Logo.svg", "Streaming"),
        PopularService("apple_tv", "Apple TV+", "https://upload.wikimedia.org/wikipedia/commons/7/77/Apple_TV_plus_logo.svg", "Streaming"),
        PopularService("hulu", "Hulu", "https://upload.wikimedia.org/wikipedia/commons/e/e4/Hulu_Logo.svg", "Streaming"),
        PopularService("paramount", "Paramount+", "https://upload.wikimedia.org/wikipedia/commons/a/a5/Paramount_Plus.svg", "Streaming"),
        PopularService("peacock", "Peacock", "https://upload.wikimedia.org/wikipedia/commons/4/44/Peacock_Logo.svg", "Streaming"),
        
        // Music Services
        PopularService("apple_music", "Apple Music", "https://upload.wikimedia.org/wikipedia/commons/5/5d/Apple_Music_logo.svg", "Music"),
        PopularService("tidal", "Tidal", "https://upload.wikimedia.org/wikipedia/commons/8/8a/Tidal_logo.svg", "Music"),
        PopularService("deezer", "Deezer", "https://upload.wikimedia.org/wikipedia/commons/8/84/Deezer_logo.svg", "Music"),
        PopularService("pandora", "Pandora", "https://upload.wikimedia.org/wikipedia/commons/2/2a/Pandora_Media_logo.svg", "Music"),
        PopularService("soundcloud", "SoundCloud Go", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Soundcloud_logo.svg", "Music"),
        
        // Cloud Storage
        PopularService("google_one", "Google One", "https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg", "Storage"),
        PopularService("dropbox", "Dropbox", "https://upload.wikimedia.org/wikipedia/commons/7/78/Dropbox_Icon.svg", "Storage"),
        PopularService("icloud", "iCloud", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg", "Storage"),
        PopularService("onedrive", "OneDrive", "https://upload.wikimedia.org/wikipedia/commons/8/8e/Microsoft_OneDrive_logo.svg", "Storage"),
        PopularService("box", "Box", "https://upload.wikimedia.org/wikipedia/commons/7/7a/Box_%28company%29_logo.svg", "Storage"),
        
        // Software & Productivity
        PopularService("adobe_creative", "Adobe Creative Cloud", "https://upload.wikimedia.org/wikipedia/commons/7/7b/Adobe_Systems_logo_and_wordmark.svg", "Software"),
        PopularService("microsoft_365", "Microsoft 365", "https://upload.wikimedia.org/wikipedia/commons/4/44/Microsoft_logo.svg", "Software"),
        PopularService("notion", "Notion", "https://upload.wikimedia.org/wikipedia/commons/4/45/Notion_app_logo.png", "Productivity"),
        PopularService("evernote", "Evernote", "https://upload.wikimedia.org/wikipedia/commons/8/8a/Evernote_Icon.svg", "Productivity"),
        PopularService("todoist", "Todoist", "https://upload.wikimedia.org/wikipedia/commons/7/7f/Todoist_logo.svg", "Productivity"),
        
        // AI & Tech
        PopularService("chatgpt", "ChatGPT Plus", "https://upload.wikimedia.org/wikipedia/commons/0/04/ChatGPT_logo.svg", "AI"),
        PopularService("midjourney", "Midjourney", "https://www.midjourney.com/favicon.ico", "AI"),
        PopularService("github_copilot", "GitHub Copilot", "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png", "Development"),
        PopularService("figma", "Figma", "https://upload.wikimedia.org/wikipedia/commons/3/33/Figma-logo.svg", "Design"),
        PopularService("canva", "Canva Pro", "https://upload.wikimedia.org/wikipedia/commons/0/08/Canva_icon_2021.svg", "Design"),
        
        // Gaming
        PopularService("xbox_game_pass", "Xbox Game Pass", "https://upload.wikimedia.org/wikipedia/commons/f/f9/Xbox_one_logo.svg", "Gaming"),
        PopularService("playstation_plus", "PlayStation Plus", "https://upload.wikimedia.org/wikipedia/commons/0/00/PlayStation_logo.svg", "Gaming"),
        PopularService("nintendo_switch_online", "Nintendo Switch Online", "https://upload.wikimedia.org/wikipedia/commons/0/0d/Nintendo.svg", "Gaming"),
        PopularService("steam", "Steam", "https://upload.wikimedia.org/wikipedia/commons/8/83/Steam_icon_logo.svg", "Gaming"),
        PopularService("epic_games", "Epic Games", "https://upload.wikimedia.org/wikipedia/commons/3/31/Epic_Games_logo.svg", "Gaming"),
        
        // Fitness & Health
        PopularService("strava", "Strava", "https://upload.wikimedia.org/wikipedia/commons/2/26/Strava_Logo.svg", "Fitness"),
        PopularService("myfitnesspal", "MyFitnessPal", "https://www.myfitnesspal.com/favicon.ico", "Health"),
        PopularService("headspace", "Headspace", "https://www.headspace.com/favicon.ico", "Wellness"),
        PopularService("calm", "Calm", "https://www.calm.com/favicon.ico", "Wellness"),
        PopularService("nike_training", "Nike Training Club", "https://www.nike.com/favicon.ico", "Fitness"),
        
        // News & Media
        PopularService("new_york_times", "The New York Times", "https://upload.wikimedia.org/wikipedia/commons/7/77/The_New_York_Times_logo.png", "News"),
        PopularService("washington_post", "The Washington Post", "https://upload.wikimedia.org/wikipedia/commons/6/6a/Washington_Post_logo.svg", "News"),
        PopularService("medium", "Medium", "https://upload.wikimedia.org/wikipedia/commons/e/ec/Medium_logo_Monogram.svg", "News"),
        PopularService("blinkist", "Blinkist", "https://www.blinkist.com/favicon.ico", "Learning"),
        PopularService("audible", "Audible", "https://upload.wikimedia.org/wikipedia/commons/7/7a/Audible_logo.svg", "Audiobooks"),
        
        // Food & Delivery
        PopularService("uber_eats", "Uber Eats Pass", "https://upload.wikimedia.org/wikipedia/commons/c/cc/Uber_logo_2018.svg", "Food"),
        PopularService("doordash", "DashPass", "https://upload.wikimedia.org/wikipedia/commons/3/3a/DoorDash_Logo.svg", "Food"),
        PopularService("grubhub", "Grubhub+", "https://upload.wikimedia.org/wikipedia/commons/7/79/Grubhub_logo.svg", "Food"),
        PopularService("hello_fresh", "HelloFresh", "https://www.hellofresh.com/favicon.ico", "Food"),
        PopularService("blue_apron", "Blue Apron", "https://www.blueapron.com/favicon.ico", "Food"),
        
        // Shopping
        PopularService("amazon_prime_shopping", "Amazon Prime (Shopping)", "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg", "Shopping"),
        PopularService("costco", "Costco", "https://upload.wikimedia.org/wikipedia/commons/5/59/Costco_Wholesale_logo_2010-10-26.svg", "Shopping"),
        PopularService("walmart_plus", "Walmart+", "https://upload.wikimedia.org/wikipedia/commons/c/ca/Walmart_logo.svg", "Shopping"),
        
        // Communication
        PopularService("zoom", "Zoom", "https://upload.wikimedia.org/wikipedia/commons/7/7b/Zoom_Communications_Logo.svg", "Communication"),
        PopularService("slack", "Slack", "https://upload.wikimedia.org/wikipedia/commons/d/d5/Slack_icon_2019.svg", "Communication"),
        PopularService("discord", "Discord Nitro", "https://upload.wikimedia.org/wikipedia/commons/9/98/Discord_logo.svg", "Communication"),
        PopularService("teams", "Microsoft Teams", "https://upload.wikimedia.org/wikipedia/commons/c/c9/Microsoft_Office_Teams_%282018%E2%80%93present%29.svg", "Communication"),
        
        // Security & VPN
        PopularService("nordvpn", "NordVPN", "https://upload.wikimedia.org/wikipedia/commons/2/20/NordVPN_logo.svg", "VPN"),
        PopularService("expressvpn", "ExpressVPN", "https://www.expressvpn.com/favicon.ico", "VPN"),
        PopularService("surfshark", "Surfshark", "https://www.surfshark.com/favicon.ico", "VPN"),
        PopularService("1password", "1Password", "https://upload.wikimedia.org/wikipedia/commons/0/00/1Password_icon.svg", "Security"),
        PopularService("lastpass", "LastPass", "https://upload.wikimedia.org/wikipedia/commons/7/7a/LastPass_logo.svg", "Security"),
        
        // Learning & Education
        PopularService("coursera", "Coursera Plus", "https://upload.wikimedia.org/wikipedia/commons/9/97/Coursera-Logo_600x600.svg", "Education"),
        PopularService("udemy", "Udemy", "https://upload.wikimedia.org/wikipedia/commons/7/82/Udemy_Logo.svg", "Education"),
        PopularService("skillshare", "Skillshare", "https://www.skillshare.com/favicon.ico", "Education"),
        PopularService("masterclass", "MasterClass", "https://www.masterclass.com/favicon.ico", "Education"),
        PopularService("linkedin_learning", "LinkedIn Learning", "https://upload.wikimedia.org/wikipedia/commons/c/ca/LinkedIn_logo_initials.png", "Education"),
        
        // Photography
        PopularService("adobe_lightroom", "Adobe Lightroom", "https://upload.wikimedia.org/wikipedia/commons/7/7b/Adobe_Systems_logo_and_wordmark.svg", "Photography"),
        PopularService("vsco", "VSCO", "https://vsco.co/favicon.ico", "Photography"),
        PopularService("unsplash", "Unsplash", "https://unsplash.com/favicon.ico", "Photography"),
        
        // Finance
        PopularService("mint", "Mint", "https://www.mint.com/favicon.ico", "Finance"),
        PopularService("ynab", "YNAB", "https://www.ynab.com/favicon.ico", "Finance"),
        PopularService("pocketguard", "PocketGuard", "https://pocketguard.com/favicon.ico", "Finance"),
        
        // Travel
        PopularService("airbnb", "Airbnb Plus", "https://upload.wikimedia.org/wikipedia/commons/6/69/Airbnb_Logo_B%C3%A9lo.svg", "Travel"),
        PopularService("booking", "Booking.com", "https://upload.wikimedia.org/wikipedia/commons/6/63/Booking.com_logo.svg", "Travel"),
        PopularService("expedia", "Expedia", "https://upload.wikimedia.org/wikipedia/commons/a/a0/Expedia_2012_logo.svg", "Travel"),
        
        // Social Media
        PopularService("twitter_blue", "Twitter Blue", "https://upload.wikimedia.org/wikipedia/commons/6/6f/Logo_of_Twitter.svg", "Social"),
        PopularService("instagram", "Instagram", "https://upload.wikimedia.org/wikipedia/commons/a/a5/Instagram_icon.png", "Social"),
        PopularService("linkedin_premium", "LinkedIn Premium", "https://upload.wikimedia.org/wikipedia/commons/c/ca/LinkedIn_logo_initials.png", "Social"),
        
        // Utilities
        PopularService("grammarly", "Grammarly", "https://upload.wikimedia.org/wikipedia/commons/7/7e/Grammarly_logo.svg", "Writing"),
        PopularService("bear", "Bear", "https://bear.app/favicon.ico", "Notes"),
        PopularService("day_one", "Day One", "https://dayoneapp.com/favicon.ico", "Journaling"),
        PopularService("fantastical", "Fantastical", "https://flexibits.com/favicon.ico", "Calendar"),
        PopularService("spark", "Spark", "https://sparkmailapp.com/favicon.ico", "Email"),
        
        // Additional Popular Services
        PopularService("crunchyroll", "Crunchyroll", "https://upload.wikimedia.org/wikipedia/commons/0/08/Crunchyroll_Logo.svg", "Streaming"),
        PopularService("fubo", "fuboTV", "https://www.fubo.tv/favicon.ico", "Streaming"),
        PopularService("showtime", "Showtime", "https://upload.wikimedia.org/wikipedia/commons/4/4a/Showtime_Logo.svg", "Streaming"),
        PopularService("starz", "Starz", "https://upload.wikimedia.org/wikipedia/commons/8/8a/Starz_logo.svg", "Streaming"),
        PopularService("apple_arcade", "Apple Arcade", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg", "Gaming"),
        PopularService("google_play_pass", "Google Play Pass", "https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg", "Gaming"),
        PopularService("adobe_acrobat", "Adobe Acrobat", "https://upload.wikimedia.org/wikipedia/commons/7/7b/Adobe_Systems_logo_and_wordmark.svg", "Software"),
        PopularService("autodesk", "Autodesk", "https://upload.wikimedia.org/wikipedia/commons/6/6a/Autodesk_logo.svg", "Software"),
        PopularService("salesforce", "Salesforce", "https://upload.wikimedia.org/wikipedia/commons/f/f9/Salesforce.com_logo.svg", "Business"),
        PopularService("hubspot", "HubSpot", "https://upload.wikimedia.org/wikipedia/commons/2/2f/HubSpot_Logo.svg", "Business"),
        PopularService("zendesk", "Zendesk", "https://upload.wikimedia.org/wikipedia/commons/7/7a/Zendesk_logo.svg", "Business"),
        PopularService("mailchimp", "Mailchimp", "https://upload.wikimedia.org/wikipedia/commons/b/bb/Mailchimp_logo.svg", "Marketing"),
        PopularService("shopify", "Shopify", "https://upload.wikimedia.org/wikipedia/commons/0/0e/Shopify_logo_2018.svg", "E-commerce"),
        PopularService("squarespace", "Squarespace", "https://upload.wikimedia.org/wikipedia/commons/a/a0/Squarespace_logo.svg", "Web Hosting"),
        PopularService("wix", "Wix", "https://upload.wikimedia.org/wikipedia/commons/a/a0/Wix.com_Logo_2020.svg", "Web Hosting"),
        PopularService("wordpress", "WordPress.com", "https://upload.wikimedia.org/wikipedia/commons/0/09/Wordpress_logo.svg", "Web Hosting"),
        PopularService("webflow", "Webflow", "https://webflow.com/favicon.ico", "Web Design"),
        PopularService("sentry", "Sentry", "https://sentry.io/favicon.ico", "Development"),
        PopularService("datadog", "Datadog", "https://www.datadoghq.com/favicon.ico", "Monitoring"),
        PopularService("new_relic", "New Relic", "https://newrelic.com/favicon.ico", "Monitoring"),
        PopularService("cloudflare", "Cloudflare", "https://upload.wikimedia.org/wikipedia/commons/9/94/Cloudflare_Logo.svg", "CDN"),
        PopularService("aws", "Amazon Web Services", "https://upload.wikimedia.org/wikipedia/commons/b/bc/Amazon-S3-Logo.svg", "Cloud"),
        PopularService("google_cloud", "Google Cloud", "https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg", "Cloud"),
        PopularService("azure", "Microsoft Azure", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Microsoft_Azure.svg", "Cloud")
    )
}

