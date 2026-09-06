import bedroomWithSleepClothes from "./images/bedroomwithsleepclothes.jpg";
import bedroomWithUnderwear from "./images/bedroomwithunderwear.jpg";
import bedroom from "./images/bedroom.jpg";
import cityWalk from "./images/citywalk.jpg";
import sunset from "./images/sunset.jpg";
import flowers from "./images/flowers.jpg";
import bedroomWithUnderwearSexy from "./images/bedrromwithunderwearsexy.jpg";
import cleaning from "./images/cleaning.jpg";
import cooking from "./images/cooking.jpg";

export type ImageCategory = "calm" | "nature" | "cozy" | "bright" | "night";

export type LisaImage = {
  id: string;
  category: ImageCategory;
  title: string;
  alt: string;
  url: string;
};

export const imageDatabase: LisaImage[] = [
  {
    id: "bedroom-sleep-clothes",
    category: "calm",
    title: "A quiet bedroom",
    alt: "A quiet bedroom with sleep clothes",
    url: bedroomWithSleepClothes.src,
  },
  {
    id: "bedroom-underwear",
    category: "cozy",
    title: "Getting ready",
    alt: "A bedroom with clothes prepared for the day",
    url: bedroomWithUnderwear.src,
  },
  {
    id: "bedroom",
    category: "cozy",
    title: "A soft room",
    alt: "A softly lit bedroom",
    url: bedroom.src,
  },
  {
    id: "city-walk",
    category: "nature",
    title: "A city walk",
    alt: "A walk through the city",
    url: cityWalk.src,
  },
  {
    id: "sunset",
    category: "night",
    title: "Sunset",
    alt: "The sky at sunset",
    url: sunset.src,
  },
  {
    id: "flowers",
    category: "nature",
    title: "Flowers",
    alt: "A group of flowers",
    url: flowers.src,
  },
  {
    id: "bedroom-underwear-sexy",
    category: "cozy",
    title: "An intimate evening",
    alt: "An intimate bedroom scene",
    url: bedroomWithUnderwearSexy.src,
  },
  {
    id: "cleaning",
    category: "cozy",
    title: "A fresh room",
    alt: "Lisa tidying a bright living room",
    url: cleaning.src,
  },
  {
    id: "cooking",
    category: "cozy",
    title: "Cooking together",
    alt: "Lisa preparing vegetables in a warm kitchen",
    url: cooking.src,
  },
];

const imageKeywords: Record<ImageCategory, string[]> = {
  calm: ["calm", "quiet", "peace", "relax", "ruhig", "entspann"],
  nature: ["nature", "flower", "garden", "forest", "meer", "natur", "blume", "garten"],
  cozy: ["cozy", "tea", "coffee", "warm", "gemüt", "tee", "kaffee", "cook", "cooking", "kitchen", "kochen", "küche", "clean", "cleaning", "tidy", "putz", "aufräum"],
  bright: ["sun", "light", "bright", "morning", "licht", "sonne", "morgen"],
  night: ["night", "evening", "sleep", "moon", "nacht", "abend", "schlaf"],
};

export function getImageForMessage(message: string, messageNumber: number) {
  const normalizedMessage = message.toLocaleLowerCase();
  const wantsImage = ["image", "picture", "photo", "show me", "bild", "foto", "zeig"].some((keyword) => normalizedMessage.includes(keyword));
  const matchingCategory = (Object.keys(imageKeywords) as ImageCategory[]).find((category) => imageKeywords[category].some((keyword) => normalizedMessage.includes(keyword)));

  if (!wantsImage && !matchingCategory) return undefined;
  const matchingImages = matchingCategory ? imageDatabase.filter((image) => image.category === matchingCategory) : imageDatabase;
  return matchingImages[messageNumber % matchingImages.length];
}
