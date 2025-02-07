module.exports = {
  apps: [
    {
      name: "lacocinadeemilia_BE",
      script: "mvn",
      args: "jetty:run -Denv=production -Pproduction -DskipTests",
      interpreter: "none", // Esto asegura que PM2 no intente ejecutarlo como un script de Node.js
    },
  ],
};
