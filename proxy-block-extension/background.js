const proxyBlockErrors = [
  "net::ERR_CONNECTION_CLOSED",
  "net::ERR_CONNECTION_RESET",
  "net::ERR_TUNNEL_CONNECTION_FAILED",
  "net::ERR_PROXY_CONNECTION_FAILED"
];

chrome.webNavigation.onErrorOccurred.addListener((details) => {
  if (details.frameId !== 0) return;

  if (!details.url.startsWith("https://")) return;

  if (!proxyBlockErrors.includes(details.error)) return;

  let hostname = "sitio desconocido";

  try {
    const url = new URL(details.url);
    hostname = url.hostname;
  } catch (e) {
    console.error("URL inválida:", details.url);
  }

  const blockedPage = chrome.runtime.getURL(
    "blocked.html?site=" + encodeURIComponent(hostname)
  );

  chrome.tabs.update(details.tabId, {
    url: blockedPage
  });
});