import { onBeforeUnmount, ref } from "vue";

// Below this width the app uses its phone layout. Keep in sync with the @media (max-width: 767px) rules.
export const MOBILE_QUERY = "(max-width: 767px)";

// Reactive "is the screen phone-sized?" for things CSS alone can't change (dialog widths, drawers, pagination).
export function useIsMobile() {
  const media = window.matchMedia(MOBILE_QUERY);
  const isMobile = ref(media.matches);
  const update = (event) => (isMobile.value = event.matches);
  media.addEventListener("change", update);
  onBeforeUnmount(() => media.removeEventListener("change", update));
  return isMobile;
}
