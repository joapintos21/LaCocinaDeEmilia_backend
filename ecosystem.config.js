module.exports = {
    apps: [{
      name: "lacocinadeemilia_BE",
      script: "mvn",
      args: "jetty:run",
      instances: "max",
      autorestart: true,
      watch: false,
      // Delay between restart
      watch_delay: 1000,
      ignore_watch : ["target", ".git", "*.log"],
      max_memory_restart: "6G",
      exec_mode: "cluster"
   }]
  }