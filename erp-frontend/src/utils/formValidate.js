import dayjs from "dayjs";

// formate data function
export const formatDate = () => {
    const now = new Date();
    let hour = now.getHours();
    let minute = now.getMinutes();
    let second = now.getSeconds();
    
    // add 0 to single digit numbers
    if (hour < 10) hour = `0${hour}`;
    if (minute < 10) minute = `0${minute}`;
    if (second < 10) second = `0${second}`;
    
    return `${hour}:${minute}:${second}`;
  };
  

  // Date ranges for the report tabs, as "YYYY-MM-DD" strings for the API.
  // Built with dayjs from calendar dates. The old code parsed new Date().toLocaleDateString(), which gives
  // "24/11/2026" in en-GB browsers: JavaScript can't parse that, so every range became NaN.
  const DAY_FORMAT = "YYYY-MM-DD";
  const range = (start, end) => [start.format(DAY_FORMAT), end.format(DAY_FORMAT)];
  const today = () => dayjs().startOf("day");

  // yesterday only
  export const get1stAndToday = () => {
    const yesterday = today().subtract(1, "day");
    return range(yesterday, yesterday);
  };

  // yesterday and today, as "YYYY.MM.DD"
  export const getday = () => {
    const t = today();
    return [t.subtract(1, "day").format("YYYY.MM.DD"), t.format("YYYY.MM.DD")];
  };

  // the 7 days before today
  export const past7Day = () => range(today().subtract(7, "day"), today().subtract(1, "day"));

  // the 30 days before today
  export const past30Day = () => range(today().subtract(30, "day"), today().subtract(1, "day"));

  // this week, Monday to Sunday (dayjs counts Sunday as day 0, so Sunday is 6 days after Monday)
  export const pastWeek = () => {
    const monday = today().subtract((today().day() + 6) % 7, "day");
    return range(monday, monday.add(6, "day"));
  };

  // this month, 1st to last day
  export const pastMonth = () => range(today().startOf("month"), today().endOf("month"));
